package dev.wonyoung.infrastructure.container.aop;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * ByteBuddy를 사용하여 빈의 서브클래스 프록시를 생성하는 팩토리.
 *
 * <h2>ByteBuddy 프록시 원리</h2>
 * <p>ByteBuddy는 바이트코드 조작으로 대상 클래스를 상속한 서브클래스를 런타임에 생성한다.
 * 모든 public 메서드 호출은 {@link ByteBuddyInterceptorDelegate}로 위임되고,
 * 위임 대상에서 {@code @SuperCall Callable}을 통해 원본 메서드(super)를 호출한다.</p>
 *
 * <h2>CGLIB 대비 장점</h2>
 * <p>CGLIB 3.x는 {@code ClassLoader.defineClass}에 리플렉션으로 접근하므로
 * Java 9+ 모듈 시스템에서 {@code --add-opens} 없이 동작하지 않는다.
 * ByteBuddy는 {@code ClassLoadingStrategy.UsingLookup}으로
 * {@code MethodHandles.Lookup.defineClass()}를 사용하므로 추가 JVM 옵션이 필요 없다.</p>
 *
 * <h2>프록시 적용 조건</h2>
 * <ul>
 *   <li>인터셉터가 하나라도 등록되어 있어야 프록시를 생성한다</li>
 *   <li>{@code final} 클래스는 상속 불가이므로 프록시를 적용하지 않고 일반 인스턴스를 반환한다</li>
 * </ul>
 */
public class ProxyFactory {

    private final List<MethodInterceptor> interceptors;

    public ProxyFactory(List<MethodInterceptor> interceptors) {
        this.interceptors = interceptors;
    }

    /**
     * 주어진 클래스의 인스턴스를 생성한다. 인터셉터가 등록되어 있고 {@code final}이 아닌 클래스라면
     * ByteBuddy 서브클래스 프록시로 감싸서 반환하고, 그렇지 않으면 일반 인스턴스를 반환한다.
     *
     * @param clazz    생성할 클래스
     * @param argTypes 사용할 생성자의 파라미터 타입 배열 (no-arg이면 빈 배열)
     * @param args     생성자에 전달할 인자 배열 (no-arg이면 빈 배열)
     * @return 생성된 인스턴스 또는 ByteBuddy 프록시
     */
    public Object createProxy(Class<?> clazz, Class<?>[] argTypes, Object[] args) throws Exception {
        if (interceptors.isEmpty() || Modifier.isFinal(clazz.getModifiers())) {
            return newInstance(clazz, argTypes, args);
        }

        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());

        Class<?> proxyClass = new ByteBuddy()
                .subclass(clazz)
                .method(ElementMatchers.isPublic())
                .intercept(MethodDelegation.to(new ByteBuddyInterceptorDelegate(interceptors)))
                .make()
                .load(clazz.getClassLoader(), ClassLoadingStrategy.UsingLookup.of(lookup))
                .getLoaded();

        return newInstance(proxyClass, argTypes, args);
    }

    private Object newInstance(Class<?> clazz, Class<?>[] argTypes, Object[] args) throws Exception {
        if (argTypes.length == 0) {
            return clazz.getDeclaredConstructor().newInstance();
        }
        return clazz.getDeclaredConstructor(argTypes).newInstance(args);
    }
}
