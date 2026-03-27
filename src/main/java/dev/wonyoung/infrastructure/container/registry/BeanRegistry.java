package dev.wonyoung.infrastructure.container.registry;

import java.util.HashMap;
import java.util.Map;

/**
 * 생성된 빈 인스턴스를 저장하고 타입으로 조회하는 레지스트리.
 *
 * <h2>저장 구조</h2>
 * <p>내부적으로 {@code Map<Class<?>, Object>}를 사용한다.
 * 키는 빈의 구체 클래스(또는 CGLIB 프록시의 슈퍼클래스)이고, 값은 실제 인스턴스(또는 프록시)다.</p>
 *
 * <h2>타입 조회 방식</h2>
 * <p>{@link #findByType(Class)}는 저장된 모든 인스턴스를 순회하며
 * {@code type.isAssignableFrom(obj.getClass())}로 타입 호환성을 검사한다.
 * 덕분에 구체 클래스뿐만 아니라 인터페이스나 상위 클래스 타입으로도 빈을 조회할 수 있다.</p>
 *
 * <p>예: {@code UserRepository} 인터페이스를 구현한 {@code JdbcUserRepository} 빈이 저장되어 있을 때,
 * {@code findByType(UserRepository.class)}로 조회하면 해당 인스턴스가 반환된다.</p>
 *
 * <h2>책임 범위</h2>
 * <p>이 클래스는 오직 빈의 저장과 조회만 담당한다.
 * 빈 생성, 의존성 주입, 순환 의존성 감지에는 관여하지 않는다.</p>
 */
public class BeanRegistry {

    private final Map<Class<?>, Object> store = new HashMap<>();

    /**
     * 빈 인스턴스를 레지스트리에 저장한다.
     *
     * @param type     빈의 구체 클래스 타입 (키로 사용)
     * @param instance 저장할 빈 인스턴스 (CGLIB 프록시일 수 있음)
     */
    public void put(Class<?> type, Object instance) {
        store.put(type, instance);
    }

    /**
     * 주어진 타입에 호환되는 빈 인스턴스를 반환한다.
     *
     * <p>저장된 모든 인스턴스를 순회하며 {@code type.isAssignableFrom(obj.getClass())}를 검사한다.
     * CGLIB 프록시의 경우 프록시 클래스가 원본 클래스를 상속하므로 이 검사를 통과한다.</p>
     *
     * @param type 조회할 타입 (구체 클래스, 인터페이스, 상위 클래스 모두 가능)
     * @return 호환되는 빈 인스턴스, 없으면 {@code null}
     */
    public Object findByType(Class<?> type) {
        return store.values().stream()
                .filter(obj -> type.isAssignableFrom(obj.getClass()))
                .findFirst()
                .orElse(null);
    }
}
