package com.g7.framework.aspect.check;

import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.chinaway.router.service.WebMethod;
import com.g7.framework.common.dto.BaseResult;
import com.g7.framework.common.dto.Request;
import com.g7.framework.framwork.exception.BusinessException;
import com.g7.framework.framwork.exception.meta.CodeMeta;
import com.g7.framework.framwork.exception.meta.CommonErrorCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * @author dreamyao
 * @title
 * @date 2018/8/25 下午2:00
 * @since 1.0.0
 */
@Aspect
public class CheckInterceptAspect {

    private static Logger logger = LoggerFactory.getLogger(CheckInterceptAspect.class);

    @Around("@within(com.alibaba.dubbo.config.annotation.Service)")
    public Object around(ProceedingJoinPoint joinPoint) {

        String clazzName = joinPoint.getTarget().getClass().getName();
        String mName = joinPoint.getSignature().getName();
        Class<?> returnType = ((MethodSignature) joinPoint.getSignature()).getReturnType();
        Object[] args = joinPoint.getArgs();
        Object returnObj;
        long start = System.currentTimeMillis();

        try {
            //请求日志
            printRequestLog(clazzName, mName, args);
            //参数 校验
            checkParam(joinPoint);
            //执行方法
            returnObj = joinPoint.proceed();

        } catch (JSR303CheckException e) {

            logger.info("Parameter check exception, exception is ", e);
            returnObj = getResultObj(returnType, false, CommonErrorCode.ILLEGAL_ARGUMENT.getCode()
                    , e.getMessage());

        } catch (BusinessException e) {

            logger.info("Business exception, exception is {}", e.getMessage(), e);

            Boolean isShow = e.getShow();
            if (isShow) {
                returnObj = getResultObj(returnType, false, e.getErrorCode(), e.getMessage());
            } else {
                returnObj = getResultObj(returnType, false, e.getErrorCode(), "Business exception");
            }

        } catch (RpcException rpc) {

            // dubbo 调用异常
            if (rpc.isTimeout()) {
                logger.error("Rpc call timeout exception , exception is ", rpc);
                returnObj = getResultObj(returnType, false, CommonErrorCode.BUSY_SERVICE.getCode(),
                        CommonErrorCode.BUSY_SERVICE.getMsgZhCN());
            } else if (rpc.isNetwork()) {
                logger.error("Rpc network exception , exception is ", rpc);
                returnObj = getResultObj(returnType, false, CommonErrorCode.NETWORK_CONNECT_FAILED.getCode(),
                        CommonErrorCode.NETWORK_CONNECT_FAILED.getMsgZhCN());
            } else if (rpc.isSerialization()) {
                logger.error("Rpc call serialization exception , exception is ", rpc);
                returnObj = getResultObj(returnType, false, CommonErrorCode.SERIALIZATION_EXCEPTION.getCode(),
                        CommonErrorCode.SERIALIZATION_EXCEPTION.getMsgZhCN());
            } else if (rpc.isForbidded()) {
                logger.error("Rpc call forbidden exception , exception is ", rpc);
                returnObj = getResultObj(returnType, false, CommonErrorCode.FORBIDDEN_EXCEPTION.getCode(),
                        CommonErrorCode.FORBIDDEN_EXCEPTION.getMsgZhCN());
            } else {
                logger.error("Rpc call exception , exception is ", rpc);
                returnObj = getResultObj(returnType, false, CommonErrorCode.RPC_CALL_EXCEPTION.getCode(),
                        CommonErrorCode.RPC_CALL_EXCEPTION.getMsgZhCN());
            }

        } catch (Exception e) {

            // 兼容老项目抛出的RouteException
            CodeMeta codeMeta = buildCodeMeta(e.getMessage());
            if (Objects.nonNull(codeMeta)) {
                returnObj = getResultObj(returnType, false, codeMeta.getCode(), codeMeta.getMsgZhCN());
            } else {
                returnObj = getDefaultResultObj(returnType, e);
            }

        } catch (Throwable e) {
            returnObj = getDefaultResultObj(returnType, e);
        }

        printResultLog(clazzName, mName, returnObj, System.currentTimeMillis() - start);
        return returnObj;
    }

    private Object getDefaultResultObj(Class<?> returnType, Throwable e) {
        String message = e.getMessage();
        logger.error("System exception, exception is {}", message, e);
        if (Objects.isNull(message)) {
            message = "java.lang.NullPointerException";
        }
        return getResultObj(returnType, false, CommonErrorCode.SYS_ERROR.getCode(), message);
    }

    private CodeMeta buildCodeMeta(String message) {

        if (Boolean.FALSE.equals(StringUtils.isEmpty(message)) && message.contains("[_") && message.contains("_]")) {

            int begin = message.indexOf("[_") + 2;
            int end = message.indexOf("_]");

            String errorMessage = message.substring(begin, end);
            String[] split = errorMessage.split(":");

            String code = split[0];
            if (split.length == 2) {
                return new CodeMeta(code, "ERROR", split[1]);
            }

            if (split.length > 2) {
                StringBuilder builder = new StringBuilder();
                for (int i = 1; i < split.length; i++) {
                    builder.append(split[i]);
                }
                return new CodeMeta(code, "ERROR", builder.toString());
            }

            return new CodeMeta(CommonErrorCode.REMOTE_SERVICE.getCode(), errorMessage);
        }

        return null;
    }

    private void checkParam(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {

        Object[] args = joinPoint.getArgs();
        if (args.length == 1) {
            Object obj = joinPoint.getArgs()[0];
            Object checkObj;
            if (obj instanceof Request) {
                // 如果是泛型 就检查泛型的data
                checkObj = ((Request) obj).getData();
            } else {
                checkObj = obj;
            }

            WebMethod annotation = ((MethodSignature) joinPoint.getSignature()).getMethod()
                    .getAnnotation(WebMethod.class);

            if (annotation == null && null == checkObj) {
                throw new JSR303CheckException("Request data is null");
            }
            JSR303Checker.check(checkObj);
        }
    }

    private String getClassName(Throwable e) {
        String className = e.getClass().getName();
        if (className.contains(".")) {
            className = substringAfterLast(className, ".");
        }
        return className;
    }

    private <T> T getResultObj(Class<T> cls, boolean success, String code, String desc) {
        try {
            T ret = cls.newInstance();
            if (!(ret instanceof BaseResult)) {
                throw new BusinessException(CommonErrorCode.ILLEGAL_ARGUMENT.getCode()
                        , "The return type must inherit BaseResult");
            }
            ((BaseResult) ret).setSuccess(success);
            ((BaseResult) ret).setCode(code);
            ((BaseResult) ret).setDescription(desc);
            return ret;
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }

    private void printRequestLog(String clazz, String mName, Object[] args) {

        if (args != null) {
            String request = JSON.toJSONString(args,
                    SerializerFeature.WriteDateUseDateFormat, SerializerFeature.WriteMapNullValue);
            logger.info(clazz + "  " + mName + " request is " + request);
        } else {
            logger.info(clazz + "  " + mName + " no request.");
        }
    }

    /**
     * 有参并有返回值的方法
     * @param clazz
     * @param mName
     * @param returnObj
     * @param ms
     */
    private void printResultLog(String clazz, String mName, Object returnObj, long ms) {

        if (Objects.nonNull(returnObj)) {
            String result = JSON.toJSONString(returnObj,
                    SerializerFeature.WriteDateUseDateFormat, SerializerFeature.WriteMapNullValue);
            logger.info(clazz + "  " + mName + " result is " + result + " ms is " + ms);
        } else {
            logger.info(clazz + "  " + mName + " no result.");
        }
    }

    /**
     * <p>Gets the substring after the last occurrence of a separator.
     * The separator is not returned.</p>
     * <p>
     * <p>A {@code null} string input will return {@code null}.
     * An empty ("") string input will return the empty string.
     * An empty or {@code null} separator will return the empty string if
     * the input string is not {@code null}.</p>
     * <p>
     * <p>If nothing is found, the empty string is returned.</p>
     * <p>
     * <pre>
     * StringUtils.substringAfterLast(null, *)      = null
     * StringUtils.substringAfterLast("", *)        = ""
     * StringUtils.substringAfterLast(*, "")        = ""
     * StringUtils.substringAfterLast(*, null)      = ""
     * StringUtils.substringAfterLast("abc", "a")   = "bc"
     * StringUtils.substringAfterLast("abcba", "b") = "a"
     * StringUtils.substringAfterLast("abc", "c")   = ""
     * StringUtils.substringAfterLast("a", "a")     = ""
     * StringUtils.substringAfterLast("a", "z")     = ""
     * </pre>
     * @param str       the String to get a substring from, may be null
     * @param separator the String to search for, may be null
     * @return the substring after the last occurrence of the separator,
     * {@code null} if null String input
     * @since 2.0
     */
    private String substringAfterLast(final String str, final String separator) {
        if (isEmpty(str)) {
            return str;
        }

        String EMPTY = "";
        if (isEmpty(separator)) {
            return EMPTY;
        }
        final int pos = str.lastIndexOf(separator);

        int INDEX_NOT_FOUND = -1;
        if (pos == INDEX_NOT_FOUND || pos == str.length() - separator.length()) {
            return EMPTY;
        }
        return str.substring(pos + separator.length());
    }

    private boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    private String emptyToNull(String string) {
        return isEmpty(string) ? null : string;
    }

    private <T> T firstNonNull(T first, T second) {
        return first != null ? first : checkNotNull(second);
    }

    private <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }
}
