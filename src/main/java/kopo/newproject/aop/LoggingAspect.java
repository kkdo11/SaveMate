package kopo.newproject.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Objects;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    // Pointcut to execute on all methods in classes in the controller package
    @Pointcut("execution(* kopo.newproject.controller..*.*(..))")
    public void controller() {
    }

    // Pointcut to execute on all methods in classes in the service package
    @Pointcut("execution(* kopo.newproject.service..*.*(..))")
    public void service() {
    }

    @Around("controller() || service()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // Log method execution
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        log.info("Request: {}.{}({})", className, methodName, Arrays.toString(args));

        // Log HTTP Request details for controllers
        if (RequestContextHolder.getRequestAttributes() != null) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            log.info("HTTP Method: {}, Request URI: {}", request.getMethod(), request.getRequestURI());
        }

        Object result = joinPoint.proceed();

        long endTime = System.currentTimeMillis();
        long timeTaken = endTime - startTime;

        // Log method return
        log.info("Response: {}.{}(). Result: {}. Execution Time: {}ms", className, methodName, result, timeTaken);

        return result;
    }
}
