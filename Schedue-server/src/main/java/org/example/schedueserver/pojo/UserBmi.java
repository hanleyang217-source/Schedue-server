package org.example.schedueserver.pojo;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserBmi {
    private Integer userId;
    private String username;

    private BigDecimal weight;
    private BigDecimal height;
    private BigDecimal bmi;
    private String level;


}
