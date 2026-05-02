package org.example.schedueserver.service;

import org.example.schedueserver.pojo.FeedBack;
import org.example.schedueserver.pojo.PageBean;

public interface FeedbackService {

    // 添加反馈
    void add(FeedBack feedback);

    // 管理员查询反馈列表
    PageBean<FeedBack> list(Integer pageNum, Integer pageSize);

    PageBean<FeedBack> listByUserId(Integer userId, Integer pageNum, Integer pageSize);

    // 根据ID查询反馈
    FeedBack findById(Integer id);

    // 更新反馈状态
    void updateStatus(Integer id, Integer status);

    // 管理员回复反馈
    void reply(Integer id, String reply);

    // 删除反馈
    void delete(Integer id);
}
