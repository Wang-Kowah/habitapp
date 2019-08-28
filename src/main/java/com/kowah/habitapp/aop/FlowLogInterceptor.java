package com.kowah.habitapp.aop;


import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Aspect
public class FlowLogInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(FlowLogInterceptor.class);

    /**
     * 切入点需要拦截的类方法
     */
    @Pointcut("execution(* com.kowah.habitapp.controller..*.*(..)) && (@annotation(org.springframework.web.bind.annotation.RequestMapping))")
    private void log() {
    }

    /**
     * 前置通知
     */
    @Before("log()")
    public void doAccessCheck() {
        logger.debug("Request coming!");
    }
}
