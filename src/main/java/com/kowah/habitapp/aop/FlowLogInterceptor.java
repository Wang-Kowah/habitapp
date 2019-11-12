package com.kowah.habitapp.aop;

import com.alibaba.fastjson.JSON;
import com.kowah.habitapp.utils.HttpUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class FlowLogInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(FlowLogInterceptor.class);

    private static final DateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 切入点需要拦截的类方法
     */
    @Pointcut("execution(* com.kowah.habitapp.controller..*.*(..)) && (@annotation(org.springframework.web.bind.annotation.PostMapping))")
    private void log() {
    }

    /**
     * 前置通知
     */
    @Before("log()")
    public void doBefore() {
        logger.info("Request coming!");
    }

    /**
     * 环绕通知
     */
    @Around("log()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String targetClassName = joinPoint.getTarget().getClass().getName();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String targetMethodName = method.getName();

        // 找到HttpServletRequest请求对象
        Object[] args = joinPoint.getArgs();
        HttpServletRequest request = null;
        if (args != null && args.length > 0) {
            for (Object arg : args) {
                if (arg instanceof HttpServletRequest) {
                    request = (HttpServletRequest) arg;
                    break;
                }
            }
        }

        Object result = null;
        Throwable throwable = null;
        try {
            // 方法执行
            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            throwable = t;
            throw t;
        } finally {
            long cost = System.currentTimeMillis() - start;
            flow(request, result, cost, throwable);
        }

    }

    /**
     * 流水日志
     *
     * @param request Http请求
     * @param result  返回结果String | void | Map
     * @param cost    请求耗时
     * @param t       异常对象
     */
    private void flow(HttpServletRequest request, Object result, long cost, Throwable t) {
        if (request == null) {
            return;
        }
        String path = request.getRequestURI();
        String clientIp = HttpUtil.getClientIp(request);
        Map param = dealWithReqParam(request);

        Object retCode = null;
        Object ret = null;
        if (result instanceof Map) {
            Object retcode = ((Map) result).get("retcode");
            if (retcode != null) {
                retCode = retcode;
            }

            ret = JSON.toJSONString(result);
        }

        StringBuilder builder = new StringBuilder();
        builder.append(dataFormat.format(new Date())).append(",")
                .append(request.getLocalAddr()).append(",")
                .append(clientIp).append(",")
                .append(cost).append(",")
                .append(path).append(",")
//                .append(uid).append(",")
                .append(retCode).append(",")
                .append(param).append(",")
                .append(ret).append(",")
                .append(t != null ? t.getMessage() : "");

        logger.info(builder.toString());
    }

    /**
     * 解析http原始参数为key-value形式
     */
    private Map<String, String> dealWithReqParam(HttpServletRequest request) {
        Map map = request.getParameterMap();
        Map<String, String> params = new HashMap<>();
        if (map != null) {
            for (Object key : map.keySet()) {
                Object value = map.get(key);
                if (value instanceof String[]) {
                    String[] values = (String[]) value;
                    if (values.length > 0) {
                        params.put(key.toString(), values[0]);
                    } else {
                        params.put(key.toString(), "");
                    }
                } else {
                    params.put(key.toString(), value.toString());
                }
            }
        }
        return params;
    }

}
