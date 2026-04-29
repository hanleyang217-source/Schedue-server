package org.example.schedueserver.pojo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class userUpdateRequest {

    private String username;

    private String nickname;

    private BigDecimal weight;

    private BigDecimal height;

}
