package io.shinmen.chronos.common.aspect;

import java.time.Duration;
import java.time.Instant;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class LoggingAspect {
    @Around("execution(* io.shinmen.chronos.service..*.*(..)) || execution(* io.shinmen.chronos.controller..*.*(..))")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();
            Duration duration = Duration.between(start, Instant.now());
            log.info("Method {} completed in {} ms", methodName, duration.toMillis());
            return result;
        } catch (Exception e) {
            log.error("Method {} failed: {}", methodName, e.getMessage(), e);
            throw e;
        }
    }
}
