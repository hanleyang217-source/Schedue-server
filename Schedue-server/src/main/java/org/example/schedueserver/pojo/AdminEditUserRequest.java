package org.example.schedueserver.pojo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminEditUserRequest {

    @Valid
    @NotNull(message = "用户信息不能为空")
    private User user;

    @NotNull(message = "用户ID不能为空")
    private Integer id;
}
