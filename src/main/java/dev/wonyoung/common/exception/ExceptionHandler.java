package dev.wonyoung.common.exception;

import dev.wonyoung.common.container.aop.MethodInterceptor;
import dev.wonyoung.common.container.aop.MethodInvocation;
import dev.wonyoung.common.exception.exceptions.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class ExceptionHandler implements MethodInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    private final Map<Class<?>, Function<Throwable, Object>> handlers = new LinkedHashMap<>();

    public ExceptionHandler() {
        handlers.put(BusinessException.class, e -> {
            logger.warn("[BusinessException] {}", e.getMessage());
            if (e.getCause() != null) {
                logger.warn("원인: {}", e.getCause().getMessage());
            }
            return null;
        });
        handlers.put(Exception.class, e -> {
            logger.error("[Exception] {}", e.getMessage(), e);
            return null;
        });
    }

    @Override
    public Object intercept(Object target, Method method, Object[] args, MethodInvocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        } catch (Throwable e) {
            return resolve(e);
        }
    }

    private Object resolve(Throwable e) {
        Class<?> type = e.getClass();
        while (type != null) {
            Function<Throwable, Object> handler = handlers.get(type);
            if (handler != null) {
                return handler.apply(e);
            }
            type = type.getSuperclass();
        }
        logger.error("[Unhandled] {}", e.getMessage(), e);
        return null;
    }
}