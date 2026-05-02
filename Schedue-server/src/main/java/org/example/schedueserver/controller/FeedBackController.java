package org.example.schedueserver.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.schedueserver.pojo.FeedBack;
import org.example.schedueserver.pojo.PageBean;
import org.example.schedueserver.pojo.Result;
import org.example.schedueserver.service.FeedbackService;
import org.example.schedueserver.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@RestController
@RequestMapping("/feedbacks")
@Validated
public class FeedBackController {

    @Autowired
    private FeedbackService feedbackService;

    // 用户提交反馈
    @PostMapping("/add")
    public Result add(@RequestBody @Validated AddFeedbackRequest request) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        String username = (String) map.get("username");

        FeedBack feedback = new FeedBack();
        feedback.setUserId(userId);
        feedback.setUsername(username);
        feedback.setContent(request.getContent());
        feedback.setContact(request.getContact());

        feedbackService.add(feedback);
        return Result.success();
    }

    @GetMapping("/myFeedbacks")
    public Result<PageBean<FeedBack>> myFeedbacks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");

        PageBean<FeedBack> pb = feedbackService.listByUserId(userId, pageNum, pageSize);
        return Result.success(pb);
    }

    // 管理员查询反馈列表
    @GetMapping("/adminSeeFeedback")
    public Result<PageBean<FeedBack>> adminSeeFeedback(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageBean<FeedBack> pb = feedbackService.list(pageNum, pageSize);
        return Result.success(pb);
    }

    /**
     * 管理员设置反馈状态
     * @param request 设置状态请求（id: 反馈ID, status: 状态 0-待处理 1-已处理 2-已忽略）
     * @return 成功或失败
     */
    @PutMapping("/setStatus")
    public Result setStatus(@RequestBody @Validated SetStatusRequest request) {
        if (request.getStatus() < 0 || request.getStatus() > 2) {
            return Result.error("状态值无效，只能是 0、1 或 2");
        }

        FeedBack feedback = feedbackService.findById(request.getId());
        if (feedback == null) {
            return Result.error("反馈不存在");
        }

        feedbackService.updateStatus(request.getId(), request.getStatus());
        return Result.success("状态更新成功");
    }

    // 管理员查看反馈详情
    @GetMapping("/detail")
    public Result<FeedBack> detail(@RequestParam Integer id) {
        FeedBack feedback = feedbackService.findById(id);
        if (feedback == null) {
            return Result.error("反馈不存在");
        }
        return Result.success(feedback);
    }

    // 管理员回复反馈
    @PutMapping("/reply")
    public Result reply(@RequestBody @Validated ReplyFeedbackRequest request) {
        feedbackService.reply(request.getId(), request.getReply());
        return Result.success();
    }

    // 管理员删除反馈
    @DeleteMapping("/delete")
    public Result delete(@RequestParam Integer id) {
        feedbackService.delete(id);
        return Result.success();
    }

    @Data
    static class AddFeedbackRequest {
        @NotBlank(message = "反馈内容不能为空")
        private String content;

        private String contact;
    }

    @Data
    static class ReplyFeedbackRequest {
        private Integer id;

        @NotBlank(message = "回复内容不能为空")
        private String reply;
    }

    @Data
    static class SetStatusRequest {
        @NotNull(message = "反馈ID不能为空")
        private Integer id;

        @NotNull(message = "状态不能为空")
        private Integer status;
    }

}
