package dev.wonyoung.infrastructure.container.factory;

import dev.wonyoung.infrastructure.container.aop.ProxyFactory;
import dev.wonyoung.infrastructure.container.di.Inject;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Optional;

/**
 * 클래스의 생성자를 분석하여 의존성을 주입하고 인스턴스(또는 CGLIB 프록시)를 생성한다.
 *
 * <h2>생성 전략</h2>
 * <ol>
 *   <li>{@code @Inject}가 붙은 생성자가 있으면 해당 생성자를 사용한다</li>
 *   <li>없으면 기본 생성자(no-arg)를 사용한다</li>
 * </ol>
 *
 * <h2>의존성 해결</h2>
 * <p>생성자 파라미터 타입 배열을 순회하며 {@link BeanResolver#resolve(Class)}를 호출한다.
 * {@code BeanResolver}의 실제 구현은 {@code Container::get}이므로,
 * 필요한 의존성 빈이 아직 없으면 그 자리에서 재귀적으로 생성된다.</p>
 *
 * <h2>프록시 위임</h2>
 * <p>인스턴스를 직접 {@code new}로 만들지 않고, 파라미터 타입과 인자를 {@link ProxyFactory}에 넘긴다.
 * {@code ProxyFactory}가 인터셉터 유무에 따라 일반 인스턴스 또는 CGLIB 프록시를 결정한다.</p>
 *
 * <h2>책임 범위</h2>
 * <p>이 클래스는 "어떤 생성자로, 어떤 인자로 만들 것인가"만 결정한다.
 * 프록시 생성 방식, 빈 등록, 순환 의존성 감지에는 관여하지 않는다.</p>
 */
public class BeanCreator {

    private final ProxyFactory proxyFactory;
    private final BeanResolver resolver;

    /**
     * @param proxyFactory 인스턴스 생성을 실제로 수행하는 프록시 팩토리
     * @param resolver     생성자 파라미터의 의존성을 컨테이너에서 조회하는 전략
     */
    public BeanCreator(ProxyFactory proxyFactory, BeanResolver resolver) {
        this.proxyFactory = proxyFactory;
        this.resolver = resolver;
    }

    /**
     * 주어진 클래스의 인스턴스(또는 CGLIB 프록시)를 생성한다.
     *
     * <p>{@code @Inject} 생성자가 있으면 그 생성자의 파라미터를 {@link BeanResolver}로 해결하고,
     * 없으면 no-arg 생성자를 사용한다. 최종적으로 {@link ProxyFactory#createProxy}에 위임한다.</p>
     *
     * @param clazz 생성할 클래스
     * @return 생성된 인스턴스 또는 CGLIB 프록시
     * @throws Exception 생성자 접근 실패, 의존성 해결 실패 등
     */
    public Object create(Class<?> clazz) throws Exception {
        Optional<Constructor<?>> injectConstructor = Arrays.stream(clazz.getConstructors())
                .filter(c -> c.isAnnotationPresent(Inject.class))
                .findFirst();

        if (injectConstructor.isPresent()) {
            Constructor<?> constructor = injectConstructor.get();
            Class<?>[] paramTypes = constructor.getParameterTypes();
            Object[] args = resolveArgs(paramTypes);
            return proxyFactory.createProxy(clazz, paramTypes, args);
        }

        return proxyFactory.createProxy(clazz, new Class<?>[0], new Object[0]);
    }

    /**
     * 생성자 파라미터 타입 배열을 순회하며 각 타입의 빈 인스턴스를 조회하여 배열로 반환한다.
     *
     * @param paramTypes 생성자 파라미터 타입 배열
     * @return 각 타입에 해당하는 빈 인스턴스 배열
     * @throws Exception 특정 타입의 빈을 찾거나 생성하지 못한 경우
     */
    private Object[] resolveArgs(Class<?>[] paramTypes) throws Exception {
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = resolver.resolve(paramTypes[i]);
        }
        return args;
    }
}
