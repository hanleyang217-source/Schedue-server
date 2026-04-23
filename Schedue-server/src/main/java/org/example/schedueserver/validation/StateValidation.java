package org.example.schedueserver.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.schedueserver.anno.State;

public class StateValidation implements ConstraintValidator<State, String> {
    /**
     *
     * @param s     //将来要校验的数据
     * @param constraintValidatorContext
     * @return  //如果返回false，则校验不通过，反之通过
     */


//    ----------------------------------------------------------验证器验证器验证器验证器验证器验证器验证器验证器验证器-----------------------------------------
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {

        //提供校验规则
        if(s == null){
            return false;
        }
        if(s.equals("111") || s.equals("222")) {
            return true;
        }
        return false;
    }
}
