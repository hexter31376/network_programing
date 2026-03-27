package dev.wonyoung.infrastructure.container.aop.handler;

import dev.wonyoung.application.exception.AppException;
import dev.wonyoung.infrastructure.container.aop.MethodInterceptor;
import dev.wonyoung.infrastructure.container.aop.MethodInvocation;

import java.lang.reflect.Method;

public class ExceptionHandlingInterceptor implements MethodInterceptor {

    @Override
    public Object intercept(Object target, Method method, Object[] args, MethodInvocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        } catch (AppException e) {
            System.err.println("[ERROR] " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("       원인: " + e.getCause().getMessage());
            }
            return null;
        } catch (RuntimeException e) {
            System.err.println("[ERROR] 예상치 못한 오류 (" + method.getName() + "): " + e.getMessage());
            return null;
        }
    }
}
