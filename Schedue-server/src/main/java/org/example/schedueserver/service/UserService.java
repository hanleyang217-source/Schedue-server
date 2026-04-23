package org.example.schedueserver.service;

import jakarta.validation.constraints.Pattern;
import org.example.schedueserver.pojo.PageBean;
import org.example.schedueserver.pojo.User;

import java.math.BigDecimal;


public interface UserService {


    User findByUserName(@Pattern(regexp = "^\\S{5,16}$") String username);

    void register(@Pattern(regexp = "^\\S{5,16}$") String username, @Pattern(regexp = "^\\S{5,16}$") String password);

    //更新用户基本信息
    void update(User user);

    //更新密码
    void updatePwd(String newPwd);
    //修改身高体重
    void updateHW(BigDecimal height, BigDecimal weight , Integer userId);
    //更新Bmi
    void updateBmi(BigDecimal bmi);
    //管理员查询普通用户
    PageBean<User> list(Integer pageNum, Integer pageSize);
    //管理员修改用户数据接口
    void adminEditUser(User user, Integer id);
}
