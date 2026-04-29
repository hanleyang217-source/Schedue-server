package org.example.schedueserver.pojo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequest {

    @NotEmpty(message = "用户名不能为空")
    @Pattern(regexp = "^\\S{5,16}$", message = "用户名必须是5-16位非空字符")
    private String username;

    @NotEmpty(message = "密码不能为空")
    @Pattern(regexp = "^\\S{5,16}$", message = "密码必须是5-16位非空字符")
    private String password;
}
