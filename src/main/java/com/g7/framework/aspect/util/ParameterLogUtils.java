package com.g7.framework.aspect.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dreamyao
 * @create 2018-03-27 10:37
 * @desc
 **/
public class ParameterLogUtils {

    private static final Logger logger = LoggerFactory.getLogger(ParameterLogUtils.class);

    public static void info(String str) {
        logger.info(str);
    }

    public static void info(String path, String request, String response) {
        logger.info("{} request {}, response {}", path, request, response);
    }

    public static void error(String str) {
        logger.error(str);
    }
}
