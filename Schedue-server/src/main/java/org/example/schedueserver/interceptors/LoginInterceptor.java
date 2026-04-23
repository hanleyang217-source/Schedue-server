package org.example.schedueserver.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.schedueserver.utils.JwtUtil;
import org.example.schedueserver.utils.ThreadLocalUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

//拦截器
@Component
public class LoginInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //令牌验证
        String token = request.getHeader("Authorization");
        try {
            Map<String , Object> claims =  JwtUtil.parseToken(token);
            //把业务数据存储到ThreadLocal中
            ThreadLocalUtil.set(claims);

            return true;
            //放行
        } catch (Exception e) {
            response.setStatus(401);
            return false;
            //拦截
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //清空Thread Local中的数据

        ThreadLocalUtil.remove();
    }
}
