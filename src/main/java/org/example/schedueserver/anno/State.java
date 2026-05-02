package org.example.schedueserver.anno;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.example.schedueserver.validation.StateValidation;

import java.lang.annotation.*;


//元注解
@Documented
//指定提供校验规则的类
@Constraint(
        validatedBy = {StateValidation.class}
)

//元注解
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)

public @interface State {

    //提供校验失败后的提示信息
    String message() default "state 参数只能是“已发布” 或者 “草稿”";
    //指定分组
    Class<?>[] groups() default {};
    //负载  获取State注解的附加信息
    Class<? extends Payload>[] payload() default {};
}
