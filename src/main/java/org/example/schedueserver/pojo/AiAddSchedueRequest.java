package org.example.schedueserver.pojo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiAddSchedueRequest {

    @NotBlank(message = "日程描述不能为空")
    private String description;
}
