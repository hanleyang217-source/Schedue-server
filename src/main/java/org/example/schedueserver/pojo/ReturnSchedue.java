package org.example.schedueserver.pojo;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReturnSchedue {

    private Integer id;
    private Integer createrId;
    private String schedueName;
    private String address;
    private String detail;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer role;

}
