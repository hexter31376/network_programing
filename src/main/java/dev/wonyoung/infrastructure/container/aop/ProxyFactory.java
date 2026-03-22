package dev.wonyoung.infrastructure.container.aop;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Modifier;
import java.util.List;

/**
 * CGLIB(Code Generation Library)을 사용하여 빈의 서브클래스 프록시를 생성하고,
 * 등록된 {@link MethodInterceptor} 체인을 통해 모든 메서드 호출을 가로채는 팩토리.
 *
 * <h2>CGLIB 프록시 원리</h2>
 * <p>CGLIB는 바이트코드 조작으로 대상 클래스를 상속한 서브클래스를 런타임에 동적으로 생성한다.
 * 예를 들어 {@code UserService} 빈의 프록시는 {@code UserService$$EnhancerByCGLIB$$xxxx}라는
 * 이름의 서브클래스가 된다. 모든 메서드 호출은 {@code setCallback}으로 등록한
 * {@code net.sf.cglib.proxy.MethodInterceptor}로 위임된다.</p>
 *
 * <h2>JDK Dynamic Proxy와의 차이</h2>
 * <ul>
 *   <li>JDK Proxy: 인터페이스만 프록시 가능, 인터페이스의 메서드만 가로챌 수 있음</li>
 *   <li>CGLIB: 인터페이스 없이도 구체 클래스 직접 프록시 가능, 상속이므로 구체 메서드도 가로챔</li>
 * </ul>
 *
 * <h2>프록시 적용 조건</h2>
 * <ul>
 *   <li>인터셉터가 하나라도 등록되어 있어야 프록시를 생성한다</li>
 *   <li>{@code final} 클래스는 상속 불가이므로 프록시를 적용하지 않고 일반 인스턴스를 반환한다</li>
 * </ul>
 *
 * <h2>invokeSuper vs invoke</h2>
 * <p>체인의 마지막에서 {@code methodProxy.invokeSuper(proxy, args)}를 호출한다.
 * 이는 프록시 자신(서브클래스)에서 부모 클래스(원본 빈)의 메서드를 직접 실행하는 것으로,
 * 리플렉션 없이 생성된 바이트코드로 호출되어 성능이 더 좋다.</p>
 */
public class ProxyFactory {

    private final List<MethodInterceptor> interceptors;

    /**
     * @param interceptors 메서드 호출 시 순서대로 실행할 인터셉터 목록
     */
    public ProxyFactory(List<MethodInterceptor> interceptors) {
        this.interceptors = interceptors;
    }

    /**
     * 주어진 클래스의 인스턴스를 생성한다. 인터셉터가 등록되어 있고 {@code final}이 아닌 클래스라면
     * CGLIB 서브클래스 프록시로 감싸서 반환하고, 그렇지 않으면 일반 인스턴스를 반환한다.
     *
     * <p>CGLIB {@link net.sf.cglib.proxy.Enhancer}의 {@code create(Class[], Object[])}에
     * 생성자 타입과 인자를 그대로 넘기므로, 프록시 서브클래스도 동일한 생성자로 초기화된다.</p>
     *
     * @param clazz    생성할 클래스
     * @param argTypes 사용할 생성자의 파라미터 타입 배열 (no-arg이면 빈 배열)
     * @param args     생성자에 전달할 인자 배열 (no-arg이면 빈 배열)
     * @return 생성된 인스턴스 또는 CGLIB 프록시
     */
    public Object createProxy(Class<?> clazz, Class<?>[] argTypes, Object[] args) throws Exception {
        if (interceptors.isEmpty() || Modifier.isFinal(clazz.getModifiers())) {
            if (argTypes.length == 0) {
                return clazz.getDeclaredConstructor().newInstance();
            }
            return clazz.getDeclaredConstructor(argTypes).newInstance(args);
        }

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback((net.sf.cglib.proxy.MethodInterceptor) (proxy, method, cglibArgs, methodProxy) ->
                chain(proxy, method, cglibArgs, methodProxy, 0));

        return argTypes.length == 0 ? enhancer.create() : enhancer.create(argTypes, args);
    }

    /**
     * 인덱스 기반 재귀로 인터셉터 체인을 실행한다.
     *
     * <p>{@code index}가 인터셉터 목록의 끝에 도달하면 {@code methodProxy.invokeSuper(proxy, args)}로
     * 실제 메서드를 실행한다. 그 전까지는 현재 인터셉터에게 {@code next} 람다를 넘기고,
     * 인터셉터가 {@code invocation.proceed()}를 호출하면 {@code index + 1}로 재귀한다.</p>
     *
     * @param proxy       CGLIB가 생성한 프록시 인스턴스 (invokeSuper의 수신자)
     * @param method      호출된 메서드의 리플렉션 정보 (인터셉터에게 전달)
     * @param args        메서드 인자 배열
     * @param methodProxy CGLIB의 메서드 프록시 (invokeSuper 호출에 사용)
     * @param index       현재 실행할 인터셉터의 인덱스
     * @return 최종 메서드 반환값
     */
    private Object chain(Object proxy, java.lang.reflect.Method method, Object[] args, MethodProxy methodProxy, int index) throws Throwable {
        if (index == interceptors.size()) {
            return methodProxy.invokeSuper(proxy, args);
        }
        MethodInterceptor interceptor = interceptors.get(index);
        MethodInvocation next = () -> chain(proxy, method, args, methodProxy, index + 1);
        return interceptor.intercept(proxy, method, args, next);
    }
}
