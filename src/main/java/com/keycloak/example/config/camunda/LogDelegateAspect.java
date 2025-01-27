package com.keycloak.example.config.camunda;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogDelegateAspect {

    @Before("@annotation(com.keycloak.example.config.camunda.LogDelegate)")
    public void logBefore(JoinPoint joinPoint) {
        var args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof DelegateExecution execution) {
            var signature = joinPoint.getSignature();
            var log = LoggerFactory.getLogger(signature.getDeclaringTypeName());
            var strVariables = execution.getVariables()
                    .entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            log.info("{}({}) - start - variables {}", signature.getName(), execution.getBusinessKey(), strVariables);
        }
    }

    @After("@annotation(com.keycloak.example.config.camunda.LogDelegate)")
    public void logAfter(JoinPoint joinPoint) {
        var args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof DelegateExecution execution) {
            var signature = joinPoint.getSignature();
            var log = LoggerFactory.getLogger(signature.getDeclaringTypeName());
            var strVariables = execution.getVariables()
                    .entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            log.info("{}({}) - end - variables {}", signature.getName(), execution.getBusinessKey(), strVariables);
        }
    }
}
