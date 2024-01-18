package com.g7.framework.aspect.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author  dreamyao
 * @create  2018-03-20
 * @desc
 **/
public class PerfLogUtils {

    private static final Logger logger = LoggerFactory.getLogger(PerfLogUtils.class);

    public static void info(String str) {
        logger.info(str);
    }
}
