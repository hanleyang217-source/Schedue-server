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
    public void add(AddSchedueRequest request) {
        Map<String , Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");

        Schedue schedue = new Schedue();
        schedue.setSchedueName(request.getSchedueName());
        schedue.setStartTime(request.getStartTime());
        schedue.setEndTime(request.getEndTime());
        schedue.setAddress(request.getAddress());
        schedue.setTeacherName(request.getTeacherName());

        schedue.setCreaterId(id);
        schedue.setRole(0);
        schedue.setCreateTime(LocalDateTime.now());
        schedue.setUpdateTime(LocalDateTime.now());

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
