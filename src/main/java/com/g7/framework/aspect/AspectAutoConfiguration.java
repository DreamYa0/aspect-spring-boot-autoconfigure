package com.g7.framework.aspect;

import com.g7.framework.aspect.check.CheckInterceptAspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author dreamyao
 * @title
 * @date 2018/9/14 下午11:30
 * @since 1.0.0
 */
@Configuration
public class AspectAutoConfiguration {

    /**
     * 入参校验，异常捕获封装切面
     * @return 切面实列
     */
    @Bean
    @ConditionalOnClass(value = ProceedingJoinPoint.class)
    public CheckInterceptAspect checkInterceptAspect() {
        return new CheckInterceptAspect();
    }

    /*@Bean
    @ConditionalOnMissingBean(value = GracefulShutdownTomcat.class)
    public GracefulShutdownTomcat gracefulShutdownTomcat() {
        return new GracefulShutdownTomcat();
    }

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addConnectorCustomizers(gracefulShutdownTomcat());
        return tomcat;
    }*/
}
