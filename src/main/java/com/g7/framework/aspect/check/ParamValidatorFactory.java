package com.g7.framework.aspect.check;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * @author dreamyao
 * @title 获取Validator，保证单例
 * @date 2018/05/28 下午5:46
 * @since 1.0.0
 */
public enum ParamValidatorFactory {

    /**
     * 单例
     */
    INSTANCE {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

        @Override
        public Validator getValidator() {
            return factory.getValidator();
        }
    };

    public abstract Validator getValidator();
}
