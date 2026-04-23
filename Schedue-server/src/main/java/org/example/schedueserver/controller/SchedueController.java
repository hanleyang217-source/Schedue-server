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
    public Result add(@RequestBody Schedue schedue) {
        schedueService.add(schedue);
        return Result.success();
    }


    //获取用户个人一周的日程列表并且返回,前端如果想要查询另外7天的日程只需要传入不同的LocaldateTime
    @GetMapping("/weekly")
    public Result<List<Schedue>> weekly(LocalDateTime weekStartTime) {
        LocalDateTime weekEndTime = LocalDateTime.now().plusWeeks(1);
        Map<String , Object> map = ThreadLocalUtil.get();
        Integer userId =(Integer) map.get("id");
        List<Schedue> result = schedueService.findPerWeek(weekStartTime , weekEndTime , userId);
        return Result.success(result);
    }
    //--------------------这里返回了日程的所有数据，其中Role用来是判断它是普通日程还是运动日程，前端可以通过它来进行筛选



    //获取指定一个日程详情
    @GetMapping("/single")
    public Result single(Integer id) {
        Schedue R = schedueService.findById(id);
        return Result.success(R);
    }

    //修改指定日程
    @PutMapping("/updateschedue")
    public Result updateschedue(@RequestBody Schedue schedue) {
        schedueService.updateschedue(schedue);
        return Result.success();
    }

    //删除指定日程
    @DeleteMapping("/delete")
    public Result delete(@RequestParam Integer id) {
        schedueService.delete(id);
        return Result.success();
    }

    //----------------------------------------运动日程运动日程运动日程运动日程运动日程----------------------------------------------------
    //手动添加运动日程
    @PostMapping("/addsport")
    public Result addsport(@RequestBody Schedue schedue) {
        schedueService.addsport(schedue);
        return Result.success();
    }

    //根据Bmi自动添加日程
    @PostMapping("/autoadd")
    public Result autoadd(@RequestParam LocalDateTime targetDate) {
        schedueService.autoadd(targetDate);
        return Result.success();
    }


}
