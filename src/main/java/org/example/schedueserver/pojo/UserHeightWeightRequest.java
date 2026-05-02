package org.example.schedueserver.pojo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserHeightWeightRequest {

    @NotNull(message = "身高不能为空")
    private BigDecimal height;

    @NotNull(message = "体重不能为空")
    private BigDecimal weight;
}
