
package org.example.schedueserver.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeedBack {

    private Integer id; // 主键ID

    private Integer userId; // 用户ID

    private String username; // 用户名

    private String content; // 反馈内容

    private String contact; // 联系方式（可选）

    private Integer status; // 状态：0-待处理，1-已处理，2-已忽略

    private String reply; // 管理员回复

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 创建时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime; // 更新时间
}
