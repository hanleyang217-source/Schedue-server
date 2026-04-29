package org.example.schedueserver.mapper;

import org.apache.ibatis.annotations.*;
import org.example.schedueserver.pojo.Schedue;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SchedueMapper {

    //添加日程
    @Insert("insert into schedue(schedue_name , creater_id , role , address , teacher_name , create_time , update_time , start_time , end_time)" +
            " values (#{schedueName} , #{createrId} , #{role} , #{address} , #{teacherName} , #{createTime} , #{updateTime} , #{startTime} , #{endTime})")
    void add(Schedue schedue);
    //查询每个星期的日程
    @Select("select * from schedue where creater_id = #{userId} and start_time >= #{weekStartTime} and end_time <= #{weekEndTime}")
    List<Schedue> findPerWeek(LocalDateTime weekStartTime, LocalDateTime weekEndTime, Integer userId);

    @Update("update schedue set schedue_name = #{schedueName} , address = #{address} , teacher_name = #{teacherName} , update_time = now() , start_time = #{startTime} , end_time = #{endTime}")
    void update(Schedue schedue);
    //删除指定日程
    @Delete("delete from schedue where id = #{id}")
    void delete(Integer id);
    //查询指定日程详情
    @Select("select * from schedue where id = #{id}")
    Schedue findById(Integer id);
    //添加运动日程
    @Insert("insert into schedue(schedue_name , creater_id , role , address , teacher_name , create_time , update_time , start_time , end_time)" +
            " values (#{schedueName} , #{createrId} , #{role} , #{address} , #{teacherName} , #{createTime} , #{updateTime} , #{startTime} , #{endTime})")
    void addsport(Schedue schedue);

    @Select("select * from schedue where creater_id = #{userId} and start_time >= #{dayStart} and end_time <= #{dayEnd}")
    List<Schedue> findScheduesByDateRange(Integer userId, LocalDateTime dayStart, LocalDateTime dayEnd);
}

