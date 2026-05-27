package com.sspu.hitlchat.aop;


import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(* com.sspu.hitlchat.controller.*.*(*))")
    public void pointCut() {

    }

    @Before("pointCut()")
    public void logging(JoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Object[] args = point.getArgs();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String uri = request.getRequestURI();

        String[] paramNames = signature.getParameterNames();

        if (args != null && args.length > 0) {
            log.info("{} - Parameters: {}", uri, buildParamLog(paramNames, args));
        }
    }

    private String buildParamLog(String[] paramNames, Object[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            String paramName = paramNames != null && i < paramNames.length ? paramNames[i] : "arg" + i;
            sb.append(paramName).append("=");

            if (args[i] == null) {
                sb.append("null");
            } else {
                String argStr = args[i].toString();
                sb.append(argStr.length() > 200 ? argStr.substring(0, 200) + "..." : argStr);
            }
        }
        return sb.toString();
    }
}
