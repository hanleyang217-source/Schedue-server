package org.example.schedueserver.service;

import org.example.schedueserver.pojo.AddSchedueRequest;
import org.example.schedueserver.pojo.AddSportSchedueRequest;
import org.example.schedueserver.pojo.Schedue;
import org.example.schedueserver.pojo.UpdateSchedueRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface SchedueService {
    //添加日程
    void add(AddSchedueRequest request);


    List<Schedue> findPerWeek(LocalDateTime weekStartTime, LocalDateTime weekEndTime, Integer userId);

    void updateschedue(UpdateSchedueRequest request);

    void delete(Integer id);

    Schedue findById(Integer id);

    void addsport(Schedue schedue);
    //根据用户Bmi添加日程
    void autoadd(LocalDateTime targetDate);
    //手动添加运动日程
    void addsport(AddSportSchedueRequest request);
    //查询指定日期的日程
    List<Schedue> getScheduesByDateRange(Integer userId, LocalDateTime startTime, LocalDateTime endTime);

    //获取AI日程建议
    String getAISchedueAdvice(Integer userId, LocalDateTime startTime, LocalDateTime endTime);
}



