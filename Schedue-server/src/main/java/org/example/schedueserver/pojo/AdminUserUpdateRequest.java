package org.example.schedueserver.pojo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminUserUpdateRequest {

    @NotNull(message = "用户ID不能为空")
    private Integer id;

    @NotEmpty(message = "用户名不能为空")
    private String username;

    private String password;

    private String nickname;

    @NotNull(message = "体重不能为空")
    @DecimalMin(value = "0.1", message = "体重必须大于0")
    private BigDecimal weight;

    @NotNull(message = "身高不能为空")
    @DecimalMin(value = "0.1", message = "身高必须大于0")
    private BigDecimal height;

    private BigDecimal bmi;
}
