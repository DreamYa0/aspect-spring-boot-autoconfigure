package com.g7.framework.aspect.check;

import javax.validation.ConstraintViolation;
import java.util.Set;

/**
 * @author dreamyao
 * @title
 * @date 2018/8/25 下午2:00
 * @since 1.0.0
 */
public class JSR303Checker {

    /**
     * 通过jsr303规范的注解来校验参数
     */
    public static void check(Object o) {
        Set<ConstraintViolation<Object>> constraintViolations = ParamValidatorFactory.INSTANCE.getValidator()
                .validate(o);
        validate(constraintViolations);
    }

    public static String checkAndReturn(Object o) {
        Set<ConstraintViolation<Object>> constraintViolations = ParamValidatorFactory.INSTANCE.getValidator()
                .validate(o);
        JSR303CheckException exception = null;
        if (constraintViolations != null && !constraintViolations.isEmpty()) {
            exception = new JSR303CheckException();
            for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
                exception.addError(constraintViolation.getPropertyPath().toString(),
                        constraintViolation.getMessage());
            }
        }
        if (exception != null) {
            return exception.getMessage();
        }

        return "";
    }

    /**
     * 通过jsr303规范的注解来校验参数
     * @param groups 校验groups
     */
    protected static void checkWithGroup(Object o, Class<?>... groups) {
        Set<ConstraintViolation<Object>> constraintViolations = ParamValidatorFactory.INSTANCE.getValidator()
                .validate(o, groups);
        validate(constraintViolations);
    }

    private static void validate(Set<ConstraintViolation<Object>> constraintViolations) {
        JSR303CheckException exception = null;
        if (constraintViolations != null && !constraintViolations.isEmpty()) {
            exception = new JSR303CheckException();
            for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
                exception.addError(constraintViolation.getPropertyPath().toString(),
                        constraintViolation.getMessage());
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

}
