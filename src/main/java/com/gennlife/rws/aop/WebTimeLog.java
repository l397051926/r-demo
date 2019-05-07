package com.gennlife.rws.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author lmx
 * @create 2019 19 17:53
 * @desc   打印 每个接口请求的日志时间
 **/
@Aspect
@Component
public class WebTimeLog {

    private static final Logger LOG = LoggerFactory.getLogger(WebTimeLog.class);

    ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Pointcut("execution(public * com.gennlife.rws.controller..*.*(..)) && !execution(public * com.gennlife.rws.controller.InputTaskController.inputInfo(..))")
    public void webLog(){}


    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        startTime.set(System.currentTimeMillis());
    }

    @AfterReturning(returning = "ret", pointcut = "webLog()")
    public void doAfterReturning(Object ret) throws Throwable {
        // 处理完请求，返回内容
//        LOG.info("RESPONSE : " + ret);
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        LOG.info( "URL : " + request.getRequestURL().toString() + " SPEND TIME : " + (System.currentTimeMillis() - startTime.get()));
    }
}
