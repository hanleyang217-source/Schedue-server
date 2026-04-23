package org.example.schedueserver.service;

import org.example.schedueserver.pojo.Schedue;

import java.time.LocalDateTime;
import java.util.List;

public interface SchedueService {
    //添加日程
    void add(Schedue schedue);

    List<Schedue> findPerWeek(LocalDateTime weekStartTime, LocalDateTime weekEndTime, Integer userId);

    void updateschedue(Schedue schedue);

    void delete(Integer id);

    Schedue findById(Integer id);

    void addsport(Schedue schedue);
    //根据用户Bmi添加日程
    void autoadd(LocalDateTime targetDate);

    //查询指定日期的日程
}
