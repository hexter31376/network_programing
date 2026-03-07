package dev.wonyoung.infrastructure.container;

import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 경량 IoC(Inversion of Control) 컨테이너.
 *
 * <p>동작 방식은 크게 두 단계로 나뉜다.</p>
 * <ol>
 *   <li><b>스캔 단계</b>: {@link #scan(String)}을 호출하면 지정한 패키지 하위의
 *       모든 클래스를 탐색하여 {@link Component} 어노테이션이 붙은 클래스를
 *       {@code candidates}에 등록한다. 이 시점에서는 인스턴스를 생성하지 않는다.</li>
 *   <li><b>지연 초기화 단계</b>: {@link #get(Class)}를 처음 호출하는 시점에
 *       해당 타입의 인스턴스를 생성하고 {@code registry}에 캐시한다.
 *       생성 중 의존성이 필요하면 {@link #get(Class)}를 재귀 호출하여 자동 주입한다.</li>
 * </ol>
 *
 * <p><b>순환 의존성 감지</b>: 생성 중인 클래스를 {@code inProgress}로 추적하여
 * A → B → A 같은 순환이 발생하면 즉시 예외를 던진다.</p>
 */
public class Container {

    /** 생성 완료된 Bean 인스턴스 캐시. 타입 → 인스턴스 */
    private final Map<Class<?>, Object> registry = new HashMap<>();

    /** scan()으로 발견된 @Component 후보 클래스. 타입 → 클래스(아직 미생성) */
    private final Map<Class<?>, Class<?>> candidates = new HashMap<>();

    /** 현재 생성 중인 클래스 추적용. 순환 의존성 감지에 사용 */
    private final Set<Class<?>> inProgress = new HashSet<>();

    private static final String CLASS_EXTENSION = ".class";

    /**
     * Bean을 직접 등록한다.
     *
     * <p>scan() 없이 외부에서 수동으로 인스턴스를 등록할 때 사용한다.
     * 이미 같은 타입이 등록되어 있으면 덮어쓴다.</p>
     *
     * @param type     등록할 Bean의 타입
     * @param instance 등록할 인스턴스
     * @param <T>      Bean 타입
     */
    public <T> void register(Class<T> type, T instance) {
        registry.put(type, instance);
    }

    /**
     * 지정한 타입과 호환되는 Bean을 반환한다.</br>
     * 멀티스레드 환경에서의 안정성을 위해 synchronized로 메서드 전체를 잠근다.
     *
     * <p>처리 순서는 다음과 같다.</p>
     * <ol>
     *   <li>registry에서 이미 생성된 인스턴스를 탐색한다.</li>
     *   <li>없으면 candidates에서 타입 호환 클래스를 탐색한다.</li>
     *   <li>candidates에서 찾았으면 순환 의존성 여부를 확인한다.</li>
     *   <li>인스턴스를 생성하고 registry에 캐시한 뒤 반환한다.</li>
     * </ol>
     *
     * @param type 꺼낼 Bean의 타입 (인터페이스나 상위 클래스도 가능)
     * @param <T>  Bean 타입
     * @return 타입과 호환되는 Bean 인스턴스
     * @throws RuntimeException 해당 타입의 Bean이 없거나 순환 의존성이 감지된 경우
     * @throws Exception        리플렉션을 통한 인스턴스 생성 중 오류가 발생한 경우
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> T get(Class<T> type) throws Exception {
        // 1. 이미 생성된 인스턴스 확인
        Object existing = findInRegistry(type);
        if (existing != null) {
            return (T) existing;
        }

        // 2. 후보에서 타입 호환 클래스 탐색
        Class<?> candidate = findInCandidates(type);
        if (candidate == null) {
            throw new RuntimeException("Bean not found: " + type.getSimpleName());
        }

        // 3. 순환 의존성 감지: 이미 생성 중인 클래스를 다시 요청하면 순환
        if (inProgress.contains(candidate)) {
            throw new RuntimeException("순환 의존성 감지: " + candidate.getSimpleName());
        }

        // 4. 생성 시작 표시 → 인스턴스 생성 → 완료 후 표시 해제
        inProgress.add(candidate);
        Object instance = createInstance(candidate);
        inProgress.remove(candidate);

        registry.put(candidate, instance);
        return (T) instance;
    }

    /**
     * 지정한 패키지와 하위 패키지를 재귀적으로 탐색하여
     * {@link Component} 어노테이션이 붙은 클래스를 candidates에 등록한다.
     *
     * <p>이 메서드는 클래스를 발견만 할 뿐 인스턴스를 생성하지 않는다.
     * 실제 생성은 {@link #get(Class)} 호출 시점으로 미뤄진다.</p>
     *
     * @param packageName 스캔할 루트 패키지명 (예: "dev.wonyoung")
     * @throws Exception 패키지 탐색 또는 클래스 로딩 중 오류가 발생한 경우
     */
    public void scan(String packageName) throws Exception {
        for (Class<?> clazz : findClasses(packageName)) {
            if (clazz.isAnnotationPresent(Component.class)) {
                candidates.put(clazz, clazz);
            }
        }
    }

    /**
     * 리플렉션을 사용해 클래스의 인스턴스를 생성한다.
     *
     * <p>생성자 탐색 우선순위는 다음과 같다.</p>
     * <ol>
     *   <li>{@link Inject} 어노테이션이 붙은 생성자: 파라미터 타입별로
     *       {@link #get(Class)}를 재귀 호출하여 의존성을 자동 주입한다.</li>
     *   <li>기본 생성자(인자 없음): @Inject 생성자가 없을 때 사용한다.</li>
     * </ol>
     *
     * @param clazz 생성할 클래스
     * @return 생성된 인스턴스
     * @throws Exception 생성자 호출 실패 또는 의존성 해결 실패 시
     */
    private Object createInstance(Class<?> clazz) throws Exception {
        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                Class<?>[] paramTypes = constructor.getParameterTypes();
                Object[] args = new Object[paramTypes.length];
                for (int i = 0; i < paramTypes.length; i++) {
                    args[i] = get(paramTypes[i]); // 재귀적으로 의존성 해결
                }
                return constructor.newInstance(args);
            }
        }
        return clazz.getDeclaredConstructor().newInstance();
    }

    /**
     * registry에서 지정한 타입과 호환되는 인스턴스를 탐색한다.
     *
     * <p>{@code isAssignableFrom}을 사용하므로 인터페이스나 상위 클래스 타입으로도 조회 가능하다.</p>
     *
     * @param type 탐색할 타입
     * @return 호환되는 인스턴스, 없으면 {@code null}
     */
    private Object findInRegistry(Class<?> type) {
        return registry.values().stream()
                .filter(obj -> type.isAssignableFrom(obj.getClass()))
                .findFirst()
                .orElse(null);
    }

    /**
     * candidates에서 지정한 타입과 호환되는 클래스를 탐색한다.
     *
     * <p>{@code isAssignableFrom}을 사용하므로 인터페이스나 상위 클래스 타입으로도 조회 가능하다.</p>
     *
     * @param type 탐색할 타입
     * @return 호환되는 후보 클래스, 없으면 {@code null}
     */
    private Class<?> findInCandidates(Class<?> type) {
        return candidates.keySet().stream()
                .filter(type::isAssignableFrom)
                .findFirst()
                .orElse(null);
    }

    /**
     * 지정한 패키지와 하위 패키지에서 모든 클래스를 수집한다.
     *
     * <p>패키지명을 경로로 변환한 뒤 클래스패스에서 디렉토리를 찾고,
     * {@link #collectClasses(File, String, List)}를 통해 재귀 탐색한다.</p>
     *
     * @param packageName 탐색할 패키지명
     * @return 발견된 모든 {@link Class} 목록
     * @throws IllegalArgumentException 패키지에 해당하는 디렉토리를 찾을 수 없는 경우
     * @throws Exception                클래스 로딩 또는 URI 변환 중 오류가 발생한 경우
     */
    private List<Class<?>> findClasses(String packageName) throws Exception {
        String path = packageName.replace('.', '/');
        URL url = getClass().getClassLoader().getResource(path);
        if (url == null) {
            throw new IllegalArgumentException("패키지를 찾을 수 없습니다: " + packageName);
        }
        List<Class<?>> classes = new ArrayList<>();
        collectClasses(new File(url.toURI()), packageName, classes);
        return classes;
    }

    /**
     * 디렉토리를 재귀적으로 탐색하여 {@code .class} 파일을 모두 수집한다.
     *
     * <p>하위 디렉토리를 발견하면 패키지명에 디렉토리명을 이어붙여 재귀 호출한다.
     * {@code .class} 파일을 발견하면 {@link Class#forName(String)}으로 로딩하여 목록에 추가한다.</p>
     *
     * @param dir         탐색할 디렉토리
     * @param packageName 현재 디렉토리에 해당하는 패키지명
     * @param classes     수집된 클래스를 담을 리스트
     * @throws Exception 클래스 로딩 중 오류가 발생한 경우
     */
    private void collectClasses(File dir, String packageName, List<Class<?>> classes) throws Exception {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                collectClasses(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(CLASS_EXTENSION)) {
                String className = packageName + "." + file.getName().replace(CLASS_EXTENSION, "");
                classes.add(Class.forName(className));
            }
        }
    }
}
