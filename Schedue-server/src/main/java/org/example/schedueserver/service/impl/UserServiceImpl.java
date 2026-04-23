package org.example.schedueserver.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.example.schedueserver.mapper.UserMapper;
import org.example.schedueserver.pojo.PageBean;
import org.example.schedueserver.pojo.User;
import org.example.schedueserver.service.UserService;
import org.example.schedueserver.utils.Md5Util;
import org.example.schedueserver.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl  implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User findByUserName(String username) {
        User u = userMapper.findByUserName(username);
        return u;
    }

    @Override
    public void register(String username, String password) {

        //密码加密
        String md5String = Md5Util.getMD5String(password);

        //添加
        userMapper.add(username , md5String);
    }

    @Override
    public void update(User user) {
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }

    @Override
    public void updatePwd(String newPwd) {
        Map<String , Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        userMapper.updatePwd(Md5Util.getMD5String(newPwd) , id);
    }

    @Override
    public void updateHW(BigDecimal height, BigDecimal weight , Integer userId) {
        userMapper.updateHW(height , weight , userId);
    }

    @Override
    public void updateBmi(BigDecimal bmi) {
        Map<String , Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");

        userMapper.updateBmi(bmi , id);

    }

    @Override
    public PageBean<User> list(Integer pageNum, Integer pageSize) {
        PageBean<User> pb = new PageBean<>();
        //开启分页查询功能
        PageHelper.startPage(pageNum, pageSize);

        List<User> UL = userMapper.list();
        Page<User> p = (Page<User>) UL ;

        pb.setTotal(p.getTotal());
        pb.setItems(p.getResult());
        return pb;

    }

    @Override
    public void adminEditUser(User user, Integer userId) {
        String md5String = Md5Util.getMD5String(user.getPassword());
        user.setPassword(md5String);
        userMapper.adminEditUser(user , userId);
    }
}
