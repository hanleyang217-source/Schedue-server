package org.example.schedueserver.mapper;

import org.apache.ibatis.annotations.*;
import org.example.schedueserver.pojo.FeedBack;
import org.example.schedueserver.pojo.FeedBack;

import java.util.List;

@Mapper
public interface FeedBackMapper {

    // 添加反馈
    @Insert("insert into feedback(user_id, username, content, contact, status, create_time, update_time) " +
            "values(#{userId}, #{username}, #{content}, #{contact}, #{status}, now(), now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void add(FeedBack feedback);

    @Select("select * from feedback where user_id = #{userId} order by create_time desc")
    static List<FeedBack> listByUserId(Integer userId) {
        return null;
    }

    // 管理员查询所有反馈（分页）
    @Select("select * from feedback order by create_time desc")
    List<FeedBack> list();

    // 根据ID查询反馈
    @Select("select * from feedback where id = #{id}")
    FeedBack findById(Integer id);

    // 更新反馈状态
    @Update("update feedback set status = #{status}, update_time = now() where id = #{id}")
    void updateStatus(Integer id, Integer status);

    // 管理员回复反馈
    @Update("update feedback set reply = #{reply}, status = 1, update_time = now() where id = #{id}")
    void reply(Integer id, String reply);

    // 删除反馈
    @Delete("delete from feedback where id = #{id}")
    void delete(Integer id);


}
