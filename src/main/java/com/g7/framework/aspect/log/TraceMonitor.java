package com.g7.framework.aspect.log;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TraceMonitor {

    /**
     * 总体描述
     * 只对root生效
     * @return
     */
    String description() default "method chain trace start:";

    /**
     * 每行日志描述
     * 会append到trace日志后面
     * @return
     */
    String log() default "";

    /**
     * 日志时间限制
     * 只对root生效
     * @return
     */
    long time() default 100;

    /**
     * 当前模块名称
     * 只对root生效
     * @return
     */
    String module() default "";

    /**
     * 是否打印参数值和返回值
     * @return
     */
    boolean printParameters() default false;

}
