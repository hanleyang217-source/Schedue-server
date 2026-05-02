package org.example.schedueserver.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.example.schedueserver.mapper.FeedBackMapper;
import org.example.schedueserver.pojo.FeedBack;
import org.example.schedueserver.pojo.PageBean;
import org.example.schedueserver.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class FeedBackServiceImpl implements FeedbackService{
    @Autowired
    private FeedBackMapper feedbackMapper;

    @Override
    public void add(FeedBack feedback) {
        feedback.setStatus(0); // 默认待处理
        feedbackMapper.add(feedback);
    }

    @Override
    public PageBean<FeedBack> list(Integer pageNum, Integer pageSize) {
        PageBean<FeedBack> pb = new PageBean<>();
        PageHelper.startPage(pageNum, pageSize);

        List<FeedBack> list = feedbackMapper.list();
        Page<FeedBack> p = (Page<FeedBack>) list;

        pb.setTotal(p.getTotal());
        pb.setItems(p.getResult());
        return pb;
    }

    @Override
    public FeedBack findById(Integer id) {
        return feedbackMapper.findById(id);
    }

    @Override
    public void updateStatus(Integer id, Integer status) {
        feedbackMapper.updateStatus(id, status);
    }

    @Override
    public void reply(Integer id, String reply) {
        feedbackMapper.reply(id, reply);
    }

    @Override
    public void delete(Integer id) {
        feedbackMapper.delete(id);
    }

    @Override
    public PageBean<FeedBack> listByUserId(Integer userId, Integer pageNum, Integer pageSize) {
        PageBean<FeedBack> pb = new PageBean<>();
        PageHelper.startPage(pageNum, pageSize);

        List<FeedBack> list = FeedBackMapper.listByUserId(userId);
        Page<FeedBack> p = (Page<FeedBack>) list;

        pb.setTotal(p.getTotal());
        pb.setItems(p.getResult());
        return pb;
    }
}
