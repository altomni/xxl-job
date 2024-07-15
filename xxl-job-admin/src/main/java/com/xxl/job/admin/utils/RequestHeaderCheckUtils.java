package com.xxl.job.admin.utils;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

public class RequestHeaderCheckUtils {

    /**
     * 检查HTTP请求头中指定的令牌是否匹配给定的值。
     *
     * @param request HttpServletRequest对象，用于获取HTTP请求信息。
     * @param tokenKey 需要检查的令牌键名。
     * @param tokenValue 期望的令牌键值。
     * @return 如果请求头中指定的令牌键值与给定的值相匹配，则返回true；否则返回false。
     */
    public static boolean checkRequestHeaderAccess(HttpServletRequest request, String tokenKey, String tokenValue) {
        if (request.getHeader(tokenKey) != null && !request.getHeader(tokenKey).equals(tokenValue)) {
            return false;
        }
        return true;
    }

    /**
     * 检查HTTP请求头中是否包含指定的Token键。
     *
     * @param request HttpServletRequest对象，代表一个HTTP请求。
     * @param tokenKey 可变参数，表示需要检查的Token键。
     * @return boolean 如果请求头中至少包含一个指定的Token键，则返回true；否则返回false。
     */
    public static boolean checkRequestHeaderContainTokenKey(HttpServletRequest request, String... tokenKey) {
        return Arrays.stream(tokenKey).anyMatch(t -> request.getHeaders(t).hasMoreElements());
    }


}
