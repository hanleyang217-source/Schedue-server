package org.example.schedueserver.mapper;


import org.apache.ibatis.annotations.*;
import org.example.schedueserver.pojo.User;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface UserMapper {


    //根据用户名查询用户
    @Select("select  * from user where username=#{username}")
    User findByUserName(String username);
    //根据用户ID查询
    @Select("select  * from user where id = #{userId}")
    User findByUserId(Integer userId);

    //添加
    @Insert("insert into user(username,password,create_time,update_time) values(#{username}, #{password}, now(), now())")
    void add(String username, String password);
    //更新用户基本信息
    @Update("update user set nickname=#{nickname},update_time=#{updateTime},username=#{username},weight=#{weight},height=#{height} where id =#{id}")
    void update(User user);
    // 更新用户密码
    @Update("update user set password = #{Md5String} , update_time = now() where id =#{id}")
    void updatePwd(String Md5String , Integer id);
    //更新身高体重
    @Update("update user set height = #{height} , weight = #{weight} , update_time = now() where id = #{id}")
    void updateHW(BigDecimal height, BigDecimal weight ,Integer id);
    //更新用户Bmi
    @Update("update user set bmi = #{bmi} , update_time = now() where id = #{id}")
    void updateBmi(BigDecimal bmi, Integer id);
    //管理员获取用户列表
    List<User> list();
    //管理员修改用户数据
    @Update("update user set username = #{user.username} , password = #{user.password} , nickname = #{user.nickname} " +
            ", weight = #{user.weight} , height = #{user.height} , bmi = #{user.bmi}" +
            ", update_time = now() where id = #{userId}")
    void adminEditUser(@Param("user") User user, @Param("userId") Integer userId);
}
