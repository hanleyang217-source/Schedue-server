package org.example.schedueserver.controller;


import jakarta.validation.constraints.Pattern;
import org.example.schedueserver.mapper.UserMapper;
import org.example.schedueserver.pojo.*;
import org.example.schedueserver.service.UserService;
import org.example.schedueserver.utils.JwtUtil;
import org.example.schedueserver.utils.Md5Util;
import org.example.schedueserver.utils.ThreadLocalUtil;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    @Autowired
    private UserService userService;
//-------------------------------------------------登陆逻辑登陆逻辑登陆逻辑登陆逻辑----------------------------------------------------------------------------------------------
//    注册逻辑
@PostMapping("/register")
public Result register(@RequestBody @Validated LoginRequest loginRequest){
    String username = loginRequest.getUsername();
    String password = loginRequest.getPassword();

    User u = userService.findByUserName(username);
    if(u == null){
        userService.register(username,password);
        return Result.success();
    }
    else{
        return Result.error("用户名已经被使用了");
    }
}


    //        登陆逻辑
    @PostMapping("/login")
    public Result<String> login(@RequestBody @Validated LoginRequest loginRequest){
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        User loginUser = userService.findByUserName(username);

        if(loginUser == null){
            return Result.error("该用户不存在");
        }

        if(Md5Util.getMD5String(password).equals(loginUser.getPassword())){
            Map<String,Object> claims = new HashMap<>();
            claims.put("id",loginUser.getId());
            claims.put("username",loginUser.getUsername());
            claims.put("role",loginUser.getRole());
            claims.put("bmi",loginUser.getBmi());
            String token = JwtUtil.genToken(claims);
            return Result.success(token);
        }

        return Result.error("密码错误");
    }

    //获取用户角色
    @GetMapping("/userrole")
    public Result userrole(){
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userRole =(Integer) map.get("role");
        if(userRole == 1){
            return Result.success("admin");
        }
        else{
            return Result.success("normalUser");
        }
    }
//--------------------------------------用户信息操作用户信息操作用户信息操作用户信息操作用户信息操作----------------------------------------------------------------------------------
    @GetMapping("/userInfo")
    public Result <User> userInfo(/*@RequestHeader("Authorization") String token*/){
        //根据用户名查询用户
        //用户名可以在token里查询

        Map<String , Object> map = ThreadLocalUtil.get();
        String username =(String) map.get("username");
        User user = userService.findByUserName(username);
        return Result.success(user);
    }

    //更新用户基本信息
    @PutMapping("/update")
    public Result update(@RequestBody @Validated userUpdateRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setWeight(request.getWeight());
        user.setHeight(request.getHeight());

        userService.update(user);
        return Result.success("null");
    }

        //更新用户密码
    @PatchMapping("/updatePwd")
    public Result updatePwd(@RequestBody Map<String,String> params){
        //校验参数
        String oldPwd = params.get("old_pwd");
        String newPwd = params.get("new_pwd");
        String rePwd = params.get("re_pwd");

        if(!StringUtils.hasLength(oldPwd) ||  !StringUtils.hasLength(newPwd) || !StringUtils.hasLength(rePwd)){
            return Result.error("缺少必要参数");
        }

        //原密码是否正确
        //调用UserService根据用户名拿到原密码，再和oldPwd比对,调用ThreadLocal
        Map<String , Object > map = ThreadLocalUtil.get();
        String username =(String) map.get("username");
        User loginuser = userService.findByUserName(username);
        if( ! loginuser.getPassword().equals(Md5Util.getMD5String(oldPwd))){
            return Result.error("原密码不正确");
        }

        //比较rePwd和newpwd是否一样
        if( ! rePwd.equals(newPwd)){
            return Result.error("两次填写的新密码不一样");
        }

        //调用service完成更新
        userService.updatePwd(newPwd);
        return Result.success("null");
    }


    //用户修改身高体重
    @PatchMapping("/updateHW")
    public Result updateHW(@RequestBody @Validated UserHeightWeightRequest request) {
        BigDecimal height = request.getHeight();
        BigDecimal weight = request.getWeight();

        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer)map.get("id");

        if(height.compareTo(BigDecimal.ZERO) <= 0 || weight.compareTo(BigDecimal.ZERO) <= 0){
            return Result.error("信息输入不合法，身高体重不得小于等于0");
        }

        userService.updateHW(height, weight, userId);
        return Result.success();
    }
//-----------------------------------管理员逻辑管理员逻辑管理员逻辑管理员逻辑管理员逻辑-----------------------------------------------------------------------------------
//用户列表查询，仅限于role == 0
    @PostMapping("/adminSeeUser")
    public Result<PageBean<User>> list(@RequestBody @Validated PageRequest request) {
        PageBean<User> pb = userService.list(request.getPageNum(), request.getPageSize());
        return Result.success(pb);
    }

    @PutMapping("/adminEditUser")
    public Result adminEditUser(@RequestBody @Validated AdminUserUpdateRequest request){
        User user = new User();
        user.setId(request.getId());
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setNickname(request.getNickname());
        user.setWeight(request.getWeight());
        user.setHeight(request.getHeight());
        user.setBmi(request.getBmi());

        userService.adminEditUser(user, request.getId());
        return Result.success();
    }





//-----------------------------BMIBMIBMIBMIBMIBMI----------------------------------------------------------------------------------------------------------------
    //用户Bmi查询 并且更新（太懒了就整合到一起）
    @GetMapping("/bmi")
    public Result getBMI(){
        Map<String,Object> map = ThreadLocalUtil.get();
        String username =(String) map.get("username");
        User user = userService.findByUserName(username);

        UserBmi userBmi = new UserBmi();
        userBmi.setUserId(user.getId());
        userBmi.setHeight(user.getHeight());
        userBmi.setWeight(user.getWeight());

        BigDecimal heightCm = userBmi.getHeight();
        BigDecimal weight = userBmi.getWeight();

        BigDecimal heightM =  heightCm.divide(new BigDecimal(100));

        BigDecimal bmi = weight.divide(heightM, 2, BigDecimal.ROUND_HALF_UP).divide(heightM, 2, BigDecimal.ROUND_HALF_UP);

        //更新用户Bmi
        userService.updateBmi(bmi);


        // 判断 BMI 等级
        String level;
        if (bmi.compareTo(new BigDecimal("18.5")) < 0) {
            level = "偏瘦";
        } else if (bmi.compareTo(new BigDecimal("23.9")) <= 0) {
            level = "正常";
        } else if (bmi.compareTo(new BigDecimal("27.9")) <= 0) {
            level = "超重";
        } else {
            level = "肥胖";
        }

        userBmi.setLevel(level);
        return Result.success(userBmi);
    }








}
