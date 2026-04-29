package org.example.schedueserver.controller;


import jakarta.validation.constraints.Pattern;
import net.bytebuddy.asm.Advice;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.util.MapUtil;
import org.example.schedueserver.mapper.UserMapper;
import org.example.schedueserver.pojo.*;
import org.example.schedueserver.service.SchedueService;
import org.example.schedueserver.service.UserService;
import org.example.schedueserver.utils.JwtUtil;
import org.example.schedueserver.utils.Md5Util;
import org.example.schedueserver.utils.ThreadLocalUtil;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/schedue")
@Validated
public class SchedueController {

    @Autowired
    private SchedueService schedueService;

//--------------------------------------普通日程普通日程普通日程普通日程普通日程--------------------------------------------------------------------


    //添加日程 ， 这里要添加的是普通日程，前端需要把Schedue.role 设置为0 ， 用户不得修改

    @PostMapping("/add")
    public Result add(@RequestBody @Validated AddSchedueRequest request) {
        schedueService.add(request);
        return Result.success();
    }



    //获取用户个人一周的日程列表并且返回,前端如果想要查询另外7天的日程只需要传入不同的LocaldateTime
    @GetMapping("/weekly")
    public Result<List<Schedue>> weekly(@RequestParam(required = false) LocalDateTime weekStartTime) {
        if (weekStartTime == null) {
            weekStartTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        }
        LocalDateTime weekEndTime = weekStartTime.plusWeeks(1);

        Map<String , Object> map = ThreadLocalUtil.get();
        Integer userId =(Integer) map.get("id");
        List<Schedue> result = schedueService.findPerWeek(weekStartTime , weekEndTime , userId);
        return Result.success(result);
    }
    //--------------------这里返回了日程的所有数据，其中Role用来是判断它是普通日程还是运动日程，前端可以通过它来进行筛选



    //获取指定一个日程详情
    @PostMapping("/single") // 必须改为 POST 或 PUT
    public Result single(@RequestBody Map<String, Integer> params) {
        Integer id = params.get("id");
        if (id == null || id <= 0) {
            return Result.error("无效的日程ID");
        }

        Schedue schedue = schedueService.findById(id);
        if (schedue == null) {
            return Result.error("日程不存在");
        }

        return Result.success(schedue);
    }

    //修改指定日程
    @PutMapping("/updateschedue")
    public Result updateschedue(@RequestBody @Validated UpdateSchedueRequest request) {
        schedueService.updateschedue(request);
        return Result.success();
    }

    //删除指定日程
    @DeleteMapping("/delete")
    public Result delete(@RequestBody Map<String, Integer> request) {
        Integer id = request.get("id");
        if (id == null || id <= 0) {
            return Result.error("无效的日程ID");
        }
        schedueService.delete(id);
        return Result.success();
    }

    //----------------------------------------运动日程运动日程运动日程运动日程运动日程----------------------------------------------------
    //手动添加运动日程
    @PostMapping("/addsport")
    public Result addsport(@RequestBody @Validated AddSportSchedueRequest request) {
        schedueService.addsport(request);
        return Result.success();
    }

    //根据Bmi自动添加日程
    @PostMapping("/autoadd")
    public Result autoadd(@RequestBody @Validated AutoAddSchedueRequest request) {
        schedueService.autoadd(request.getTargetDate());
        return Result.success();
    }


    //获取AI日程建议
    @GetMapping("/ai-advice")
    public Result<String> getAIDataAdvice(
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {

        Map<String , Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");

        if (startTime == null) {
            startTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        }
        if (endTime == null) {
            endTime = startTime.plusWeeks(1);
        }

        String advice = schedueService.getAISchedueAdvice(userId, startTime, endTime);
        return Result.success(advice);
    }


}
