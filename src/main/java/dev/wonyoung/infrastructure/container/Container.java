package dev.wonyoung.infrastructure.container;

import dev.wonyoung.infrastructure.container.aop.MethodInterceptor;
import dev.wonyoung.infrastructure.container.aop.ProxyFactory;
import dev.wonyoung.infrastructure.container.factory.BeanCreator;
import dev.wonyoung.infrastructure.container.registry.BeanRegistry;
import dev.wonyoung.infrastructure.container.scanner.ClassScanner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DI 컨테이너의 진입점이자 퍼사드(Facade).
 *
 * <p>이 클래스는 각 전문 컴포넌트({@link ClassScanner}, {@link BeanRegistry},
 * {@link BeanCreator}, {@link ProxyFactory})를 조율하는 얇은 조율자 역할만 한다.
 * 스캔, 저장, 생성, 프록시 적용의 세부 로직은 각 컴포넌트에 위임한다.</p>
 *
 * <h2>전체 흐름</h2>
 * <pre>
 * 1. scan("dev.wonyoung")
 *      ClassScanner가 패키지를 탐색하여 @Component 클래스를 candidates에 추가
 *
 * 2. get(UserService.class)
 *      BeanRegistry에서 호환 타입 조회 → 없으면 아래로 진행
 *      candidates에서 UserService에 호환되는 클래스 탐색
 *      순환 의존성 감지 (inProgress 집합으로 추적)
 *      BeanCreator.create(candidate)
 *          @Inject 생성자 파라미터 타입을 this::get으로 재귀 해결
 *          ProxyFactory.createProxy(clazz, argTypes, args)
 *              인터셉터 있으면 CGLIB 서브클래스 프록시 생성
 *              없으면 일반 인스턴스 생성
 *      BeanRegistry에 결과 저장 (이후 같은 타입 요청 시 캐시 반환)
 * </pre>
 *
 * <h2>싱글톤 보장</h2>
 * <p>한 번 생성된 빈은 {@link BeanRegistry}에 저장되어 이후 요청에서는 재생성 없이 반환된다.
 * {@code get()} 메서드는 {@code synchronized}로 스레드 안전하게 보호된다.</p>
 *
 * <h2>순환 의존성 감지</h2>
 * <p>{@code inProgress} 집합에 현재 생성 중인 클래스를 기록한다.
 * 생성 도중 같은 클래스가 다시 요청되면 즉시 예외를 던진다.</p>
 */
public class Container {

    private final ClassScanner scanner = new ClassScanner();
    private final BeanRegistry registry = new BeanRegistry();
    private final List<MethodInterceptor> interceptors = new ArrayList<>();
    private final Set<Class<?>> candidates = new HashSet<>();
    private final Set<Class<?>> inProgress = new HashSet<>();

    /**
     * AOP 인터셉터를 등록한다. 등록 순서대로 체인이 실행된다.
     *
     * <p>인터셉터는 {@code scan()} 또는 {@code get()} 호출 전에 등록해야 한다.
     * 빈이 생성될 때 그 시점의 인터셉터 목록이 사용되기 때문이다.</p>
     *
     * @param interceptor 등록할 인터셉터
     */
    public void addInterceptor(MethodInterceptor interceptor) {
        interceptors.add(interceptor);
    }

    /**
     * 이미 생성된 인스턴스를 특정 타입으로 컨테이너에 직접 등록한다.
     *
     * <p>외부에서 직접 만든 객체(예: 설정 값, 외부 라이브러리 인스턴스)를
     * 컨테이너에 수동으로 등록할 때 사용한다. 이 경우 프록시가 적용되지 않는다.</p>
     *
     * @param type     등록할 타입 (조회 시 이 타입으로 찾을 수 있음)
     * @param instance 등록할 인스턴스
     */
    public <T> void register(Class<T> type, T instance) {
        registry.put(type, instance);
    }

    /**
     * 지정한 패키지를 스캔하여 {@code @Component} 클래스를 후보로 등록한다.
     *
     * <p>이 시점에 인스턴스가 생성되지는 않는다(지연 초기화).
     * 실제 인스턴스는 {@link #get(Class)} 호출 시점에 처음 생성된다.</p>
     *
     * @param packageName 스캔할 루트 패키지명 (예: {@code "dev.wonyoung"})
     */
    public void scan(String packageName) throws Exception {
        candidates.addAll(scanner.scan(packageName));
    }

    /**
     * 주어진 타입의 빈 인스턴스를 반환한다. 없으면 생성하고 등록한 뒤 반환한다.
     *
     * <p>구체 클래스뿐만 아니라 인터페이스나 상위 클래스 타입으로도 조회할 수 있다.
     * 인터셉터가 등록되어 있으면 CGLIB 프록시가 반환된다.</p>
     *
     * @param type 조회할 빈의 타입
     * @return 해당 타입의 빈 인스턴스 (또는 CGLIB 프록시)
     * @throws RuntimeException 빈을 찾을 수 없거나 순환 의존성이 감지된 경우
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> T get(Class<T> type) throws Exception {
        Object existing = registry.findByType(type);
        if (existing != null) {
            return (T) existing;
        }

        Class<?> candidate = findCandidate(type);
        if (candidate == null) {
            throw new RuntimeException("Bean을 찾을 수 없습니다: " + type.getSimpleName());
        }

        if (inProgress.contains(candidate)) {
            throw new RuntimeException("순환 의존성 감지: " + candidate.getSimpleName());
        }

        inProgress.add(candidate);
        ProxyFactory proxyFactory = new ProxyFactory(interceptors);
        BeanCreator creator = new BeanCreator(proxyFactory, this::get);
        Object instance = creator.create(candidate);
        inProgress.remove(candidate);

        registry.put(candidate, instance);
        return (T) instance;
    }

    /**
     * 후보 클래스 목록에서 주어진 타입에 호환되는 클래스를 찾는다.
     *
     * <p>{@code type::isAssignableFrom}을 필터로 사용하므로, 구체 클래스 일치뿐 아니라
     * 인터페이스를 구현한 클래스나 상위 클래스를 상속한 클래스도 매칭된다.</p>
     *
     * @param type 찾을 타입
     * @return 호환되는 후보 클래스, 없으면 {@code null}
     */
    private Class<?> findCandidate(Class<?> type) {
        return candidates.stream()
                .filter(type::isAssignableFrom)
                .findFirst()
                .orElse(null);
    }
}
