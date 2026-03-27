package dev.wonyoung.infrastructure.container.aop;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * ByteBuddy MethodDelegation의 대상 클래스.
 *
 * <p>ByteBuddy가 생성한 프록시 서브클래스의 모든 public 메서드 호출이
 * {@link #intercept}로 위임된다. 여기서 등록된 인터셉터 체인을 실행하고,
 * 체인 끝에서 {@code superCall.call()}로 실제 구현(super 메서드)을 호출한다.</p>
 *
 * <h2>CGLIB 대비 차이점</h2>
 * <ul>
 *   <li>CGLIB: {@code methodProxy.invokeSuper(proxy, args)}로 super 호출</li>
 *   <li>ByteBuddy: {@code @SuperCall Callable<?>}으로 super 호출 — 모듈 시스템 친화적</li>
 * </ul>
 */
public class ByteBuddyInterceptorDelegate {

    private final List<MethodInterceptor> interceptors;

    public ByteBuddyInterceptorDelegate(List<MethodInterceptor> interceptors) {
        this.interceptors = interceptors;
    }

    @RuntimeType
    public Object intercept(@This Object proxy,
                            @Origin Method method,
                            @AllArguments Object[] args,
                            @SuperCall Callable<Object> superCall) throws Throwable {
        return chain(proxy, method, args, superCall::call, 0);
    }

    private Object chain(Object proxy, Method method, Object[] args,
                         MethodInvocation superInvocation, int index) throws Throwable {
        if (index == interceptors.size()) {
            return superInvocation.proceed();
        }
        MethodInterceptor interceptor = interceptors.get(index);
        MethodInvocation next = () -> chain(proxy, method, args, superInvocation, index + 1);
        return interceptor.intercept(proxy, method, args, next);
    }
}
