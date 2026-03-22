package dev.wonyoung.infrastructure.container.aop;

import java.lang.reflect.Method;

/**
 * AOP 어드바이스(Advice)를 구현하는 인터셉터 인터페이스.
 *
 * <p>이 인터페이스를 구현하면 컨테이너가 관리하는 모든 빈의 메서드 호출을 가로채서
 * 앞뒤로 부가 로직(로깅, 트랜잭션, 성능 측정 등)을 끼워 넣을 수 있다.</p>
 *
 * <h2>실행 흐름</h2>
 * <pre>
 * 메서드 호출
 *   → ProxyFactory.chain()
 *       → interceptors[0].intercept(target, method, args, next)
 *           → invocation.proceed()  // next 호출
 *               → interceptors[1].intercept(...)
 *                   → invocation.proceed()
 *                       → methodProxy.invokeSuper()  // 실제 메서드 실행
 * </pre>
 *
 * <h2>사용 예시</h2>
 * <pre>{@code
 * container.addInterceptor((target, method, args, invocation) -> {
 *     System.out.println("[Before] " + method.getName());
 *     Object result = invocation.proceed(); // 다음 인터셉터 또는 실제 메서드로 진행
 *     System.out.println("[After] " + method.getName());
 *     return result;
 * });
 * }</pre>
 */
public interface MethodInterceptor {

    /**
     * 메서드 호출을 가로채어 부가 로직을 실행한다.
     *
     * <p>{@code invocation.proceed()}를 반드시 호출해야 다음 인터셉터 또는 실제 메서드가 실행된다.
     * 호출하지 않으면 체인이 중단된다(예외 발생 전 조기 반환 등의 용도로 활용 가능).</p>
     *
     * @param target     실제 빈 인스턴스(CGLIB 프록시 자신)
     * @param method     호출된 메서드의 리플렉션 정보
     * @param args       메서드에 전달된 인자 배열
     * @param invocation 다음 인터셉터 또는 실제 메서드로 진행하는 콜백
     * @return 메서드의 반환값 (부가 로직에서 변환 가능)
     * @throws Throwable 실제 메서드 또는 부가 로직에서 발생한 예외
     */
    Object intercept(Object target, Method method, Object[] args, MethodInvocation invocation) throws Throwable;
}
