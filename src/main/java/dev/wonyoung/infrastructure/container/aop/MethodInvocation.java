package dev.wonyoung.infrastructure.container.aop;

/**
 * 인터셉터 체인에서 다음 단계로 진행하는 콜백 인터페이스.
 *
 * <p>{@link MethodInterceptor#intercept}의 마지막 파라미터로 전달되며,
 * {@code proceed()}를 호출하면 체인의 다음 인터셉터 또는 실제 메서드가 실행된다.</p>
 *
 * <h2>체인 구조 예시</h2>
 * <pre>
 * 인터셉터가 [logging, transaction] 순으로 등록된 경우:
 *
 * chain(index=0)  →  logging.intercept(..., next0)
 *                        next0.proceed()
 *                            chain(index=1)  →  transaction.intercept(..., next1)
 *                                                   next1.proceed()
 *                                                       chain(index=2)  →  superCall.call() (실제 메서드)
 * </pre>
 *
 * <p>{@code @FunctionalInterface}는 아니지만 {@link ByteBuddyInterceptorDelegate} 내부에서
 * 람다로 구현되어 각 재귀 단계를 클로저로 캡처한다.</p>
 */
public interface MethodInvocation {

    /**
     * 인터셉터 체인의 다음 단계로 진행한다.
     *
     * <p>인터셉터 목록이 소진되면 ByteBuddy의 {@code superCall.call()}을 호출하여
     * 실제 빈의 메서드를 실행한다.</p>
     *
     * @return 실제 메서드의 반환값
     * @throws Throwable 실제 메서드 또는 이후 인터셉터에서 발생한 예외
     */
    Object proceed() throws Throwable;
}
