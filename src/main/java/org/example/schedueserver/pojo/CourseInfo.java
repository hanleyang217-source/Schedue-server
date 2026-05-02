package org.example.schedueserver.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourseInfo {
    private String courseName;      // 课程名称
    private String teacher;         // 教师
    private String location;        // 地点
    private String weekDay;         // 星期几
    private String section;         // 节次
    private LocalDateTime startTime; // 开始时间
    private LocalDateTime endTime;   // 结束时间
    private String rawInfo;         // 原始信息

    @Override
    public String toString() {
        return String.format("课程: %s | 教师: %s | 地点: %s | 时间: %s %s",
                courseName, teacher, location, weekDay, section);
    }
}
