package com.g7.framework.aspect.log;


import com.g7.framework.aspect.util.MonitorLogUtils;
import com.g7.framework.aspect.util.ParameterLogUtils;
import com.g7.framework.aspect.util.PerfLogUtils;
import com.g7.framwork.common.util.json.JsonUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author dreamyao
 */
@Aspect
public class TraceMonitorAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerfLogUtils.class);

    @Around("@annotation(com.g7.framework.aspect.log.TraceMonitor)")
    public Object aroundMethod(ProceedingJoinPoint jp) throws Throwable {

        Object result = null;
        MethodSignature joinPointObject = (MethodSignature) jp.getSignature();
        Method method = joinPointObject.getMethod();

        TraceMonitor monitor = method.getAnnotation(TraceMonitor.class);

        boolean isRoot = false;

        if (monitor != null) {

            if (null == Profiler.getEntry()) {
                Profiler.start(monitor.description());
                isRoot = true;
            }
            Profiler.enter(String.format("%s.%s %s", method.getDeclaringClass().getName(), method.getName(), monitor.log()));
        }

        try {

            result = jp.proceed();

        } catch (Throwable e) {

            finish(monitor, isRoot, method, jp, result);
            throw e;
        }

        finish(monitor, isRoot, method, jp, result);

        return result;
    }

    private void finish(TraceMonitor monitor, boolean isRoot, Method method, ProceedingJoinPoint jp, Object result) {

        if (monitor != null) {

            Profiler.release();
            if (monitor.printParameters()) {
                ParameterLogUtils.info(String.format("%s.%s", method.getDeclaringClass().getName(), method.getName())
                        , JsonUtils.toJson(jp.getArgs()), JsonUtils.toJson(result));
            }

            if (isRoot) {
                Profiler.release();
                long duration = Profiler.getDuration();
                logger.info(String.format("(%s.%s, %sms, Y)", method.getDeclaringClass().getName(), method.getName(), duration));
                MonitorLogUtils.logMonitorInfo(monitor.module(), method.getName(), MonitorLogUtils.TargetInfoType.TPS, 1);
                if (duration > monitor.time()) {
                    PerfLogUtils.info(Profiler.dump());
                }
                Profiler.reset();
            }
        }
    }
}
