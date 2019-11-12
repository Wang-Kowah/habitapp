package com.kowah.habitapp.utils;

import javax.servlet.http.HttpServletRequest;

public class HttpUtil {

    /**
     * 获取客户端ip
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = request.getRemoteAddr();
        }
        if (ip != null) {
            String[] ips = ip.split(",");
            return ips[0].trim();
        } else {
            return request.getRemoteAddr();
        }
    }
}
