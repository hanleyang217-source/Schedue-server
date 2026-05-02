package org.example.schedueserver.pojo;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class User {

    @NotNull
    private Integer id;//主键ID
    private String username;//用户名
    @JsonIgnore//让Spring把当前字符串转换为Json对象的时候忽略password,最终的Json对象就没有password
    private String password;//密码


    private String nickname;//昵称

    private Integer role; //1代表管理员 ， 0代表用户
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;//创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;//更新时间

    @NotNull
    private BigDecimal weight;//kg
    @NotNull
    private BigDecimal height; //cm

    private BigDecimal bmi;
}
