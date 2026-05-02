package org.example.schedueserver.service.impl;

import org.example.schedueserver.mapper.SchedueMapper;
import org.example.schedueserver.mapper.UserMapper;
import org.example.schedueserver.pojo.*;
import org.example.schedueserver.service.SchedueService;
import org.example.schedueserver.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class SchedueServiceImpl implements SchedueService {

    @Autowired
    private SchedueMapper schedueMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DeepSeekService deepSeekService;

    //添加日程
    @Override
    public void add(Schedue schedue) {
        Map<String , Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        schedue.setCreaterId(userId);
        schedueMapper.add(schedue);
    }

    //查询每个星期的日程
    @Override
    public List<Schedue> findPerWeek(LocalDateTime weekStartTime, LocalDateTime weekEndTime, Integer userId) {
        List<Schedue> result = schedueMapper.findPerWeek(weekStartTime , weekEndTime , userId);
        return result;
    }
    
    //修改日程
    @Override
    public void updateschedue(UpdateSchedueRequest request) {
        Schedue schedue = new Schedue();
        schedue.setId(request.getId());
        schedue.setSchedueName(request.getSchedueName());
        schedue.setStartTime(request.getStartTime());
        schedue.setEndTime(request.getEndTime());
        schedue.setAddress(request.getAddress());
        schedue.setTeacherName(request.getTeacherName());

        // 更新时间由后端自动设置，防止前端篡改
        schedue.setUpdateTime(LocalDateTime.now());

        schedueMapper.update(schedue);
    }
    //删除日程
    @Override
    public void delete(Integer id) {
        schedueMapper.delete(id);
    }
    //查询单个日程详情
    @Override
    public Schedue findById(Integer id) {
        return schedueMapper.findById(id);
    }

    @Override
    public void addsport(Schedue schedue) {
        schedueMapper.addsport(schedue);
    }

    @Override
    public List<Schedue> getScheduesByDateRange(Integer userId, LocalDateTime startTime, LocalDateTime endTime) {
        return schedueMapper.findScheduesByDateRange(userId, startTime, endTime);
    }

    @Override
    public String getAISchedueAdvice(Integer userId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Schedue> schedues = schedueMapper.findScheduesByDateRange(userId, startTime, endTime);
        return deepSeekService.getSchedueAdvice(schedues);
    }

    @Override
    public void aiAddSchedue(String description) {
        Map<String , Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");

        // 获取用户未来一周的日程列表，用于AI参考
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.toLocalDate().atTime(0, 0, 0);
        LocalDateTime weekEnd = weekStart.plusWeeks(1);

        List<Schedue> weekSchedues = schedueMapper.findScheduesByDateRange(userId, weekStart, weekEnd);

        // 让AI解析日程描述，传入一周的日程信息
        Schedue schedue = deepSeekService.parseSchedueFromDescription(description, userId, weekSchedues);

        // 根据AI返回的日程日期，查询那一整周的日程进行冲突检测
        LocalDateTime targetWeekStart = schedue.getStartTime().toLocalDate().atTime(0, 0, 0);
        LocalDateTime targetWeekEnd = targetWeekStart.plusWeeks(1);
        List<Schedue> targetWeekSchedues = schedueMapper.findScheduesByDateRange(userId, targetWeekStart, targetWeekEnd);

        // 检查并调整时间，确保不与现有日程冲突且保持15分钟间隔
        adjustScheduleWithGap(schedue, targetWeekSchedues);

        // 保存日程
        schedueMapper.add(schedue);
    }

    /**
     * 检查并调整日程时间，确保与现有日程保持最小间隔
     */
    private void adjustScheduleWithGap(Schedue newSchedue, List<Schedue> existingSchedues) {
        if (existingSchedues == null || existingSchedues.isEmpty()) {
            return;
        }

        final int MIN_GAP_MINUTES = 15;

        // 按开始时间排序现有日程
        existingSchedues.sort((s1, s2) -> s1.getStartTime().compareTo(s2.getStartTime()));

        // 检查是否与现有日程冲突或间隔不足
        for (Schedue existing : existingSchedues) {
            if (hasConflictOrInsufficientGap(newSchedue, existing, MIN_GAP_MINUTES)) {
                // 尝试调整新日程的时间
                LocalDateTime adjustedTime = findBestAvailableSlot(newSchedue, existing, MIN_GAP_MINUTES);
                if (adjustedTime != null) {
                    long durationMinutes = java.time.Duration.between(newSchedue.getStartTime(), newSchedue.getEndTime()).toMinutes();
                    newSchedue.setStartTime(adjustedTime);
                    newSchedue.setEndTime(adjustedTime.plusMinutes(durationMinutes));
                } else {
                    throw new RuntimeException("无法安排日程：当天没有足够的时间段（需要保持" + MIN_GAP_MINUTES + "分钟间隔）");
                }
            }
        }
    }

    /**
     * 检查两个日程是否有冲突或间隔不足
     */
    private boolean hasConflictOrInsufficientGap(Schedue newSchedue, Schedue existing, int minGapMinutes) {
        // 计算带间隔的时间范围
        LocalDateTime newStartWithGap = newSchedue.getStartTime().minusMinutes(minGapMinutes);
        LocalDateTime newEndWithGap = newSchedue.getEndTime().plusMinutes(minGapMinutes);

        // 判断是否重叠
        return newStartWithGap.isBefore(existing.getEndTime()) &&
                newEndWithGap.isAfter(existing.getStartTime());
    }

    /**
     * 寻找最佳可用时间段
     */
    private LocalDateTime findBestAvailableSlot(Schedue newSchedue, Schedue conflictingSchedue, int minGapMinutes) {
        long durationMinutes = java.time.Duration.between(newSchedue.getStartTime(), newSchedue.getEndTime()).toMinutes();

        // 方案1：安排在冲突日程之前
        LocalDateTime slotBefore = conflictingSchedue.getStartTime().minusMinutes(minGapMinutes).minusMinutes(durationMinutes);
        if (slotBefore.isAfter(conflictingSchedue.getStartTime().minusHours(12))) { // 确保不会太早
            return slotBefore;
        }

        // 方案2：安排在冲突日程之后（优先选择）
        LocalDateTime slotAfter = conflictingSchedue.getEndTime().plusMinutes(minGapMinutes);
        return slotAfter;
    }

    @Override
    public void addsport(AddSportSchedueRequest request) {
        Map<String , Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");

        Schedue schedue = new Schedue();
        schedue.setSchedueName(request.getSchedueName());
        schedue.setStartTime(request.getStartTime());
        schedue.setEndTime(request.getEndTime());
        schedue.setAddress(request.getAddress());
        schedue.setTeacherName(request.getTeacherName());

        // 自动设置运动日程相关字段
        schedue.setCreaterId(id);
        schedue.setRole(1); // 1 代表运动日程
        schedue.setCreateTime(LocalDateTime.now());
        schedue.setUpdateTime(LocalDateTime.now());

        schedueMapper.addsport(schedue);
    }

    @Override
    public void autoadd(LocalDateTime targetDate) {
        Map<String , Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");

        User user = userMapper.findByUserId(userId);
        if (user == null || user.getBmi() == null) {
            throw new RuntimeException("用户信息或BMI数据不存在");
        }

        BigDecimal bmi = user.getBmi();

        LocalDate targetDay = targetDate.toLocalDate();
        LocalDateTime dayStart = targetDay.atTime(9, 0);
        LocalDateTime dayEnd = targetDay.atTime(18, 0);

        List<Schedue> existingSchedues = schedueMapper.findScheduesByDateRange(userId, dayStart, dayEnd);

        LocalDateTime sportStartTime = findAvailableTimeSlot(existingSchedues, dayStart, dayEnd);

        if (sportStartTime == null) {
            throw new RuntimeException("当天9-18点没有可用的时间段");
        }

        Schedue sportSchedue = createSportSchedueBasedOnBmi(bmi, sportStartTime, userId);

        schedueMapper.addsport(sportSchedue);
    }

    private LocalDateTime findAvailableTimeSlot(List<Schedue> existingSchedues, LocalDateTime dayStart, LocalDateTime dayEnd) {
        LocalDateTime currentTime = dayStart;

        while (currentTime.isBefore(dayEnd)) {
            LocalDateTime potentialEndTime = currentTime.plusHours(1);

            if (potentialEndTime.isAfter(dayEnd)) {
                break;
            }

            boolean hasConflict = false;
            for (Schedue schedue : existingSchedues) {
                if (currentTime.isBefore(schedue.getEndTime()) && potentialEndTime.isAfter(schedue.getStartTime())) {
                    hasConflict = true;
                    currentTime = schedue.getEndTime();
                    break;
                }
            }

            if (!hasConflict) {
                return currentTime;
            }
        }

        return null;
    }

    private Schedue createSportSchedueBasedOnBmi(BigDecimal bmi, LocalDateTime startTime, Integer userId) {
        Schedue schedue = new Schedue();
        schedue.setCreaterId(userId);
        schedue.setRole(1);
        schedue.setStartTime(startTime);
        schedue.setCreateTime(LocalDateTime.now());
        schedue.setUpdateTime(LocalDateTime.now());

        double bmiValue = bmi.doubleValue();

        if (bmiValue < 18.5) {
            schedue.setSchedueName("撸管");
            schedue.setEndTime(startTime.plusMinutes(30));
            schedue.setTeacherName("91");
            schedue.setAddress("bed");
        } else if (bmiValue < 24) {
            schedue.setSchedueName("录唧唧");
            schedue.setEndTime(startTime.plusMinutes(45));
            schedue.setTeacherName("糖心");
            schedue.setAddress("chair");
        } else if (bmiValue < 28) {
            schedue.setSchedueName("录爆皮");
            schedue.setEndTime(startTime.plusMinutes(60));
            schedue.setTeacherName("JM");
            schedue.setAddress("classroom");
        } else {
            schedue.setSchedueName("戈奥迪");
            schedue.setEndTime(startTime.plusMinutes(60));
            schedue.setTeacherName("knife");
            schedue.setAddress("hospital");
        }

        return schedue;
    }


}
