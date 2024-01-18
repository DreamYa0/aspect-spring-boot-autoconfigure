package com.g7.framework.aspect.log;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author dreamyao
 * @title
 * @date 2019/3/8 3:48 PM
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
@Import(TraceMonitorBeanDefinitionRegistrar.class)
public @interface EnableMonitor {

    /**
     * 用于控制是否开启方法调用日志监控
     * @return 是否开启
     */
    boolean enable() default true;
}
