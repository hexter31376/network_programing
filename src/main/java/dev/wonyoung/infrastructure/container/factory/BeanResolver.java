package dev.wonyoung.infrastructure.container.factory;

/**
 * 특정 타입의 빈 인스턴스를 컨테이너로부터 조회하는 전략 인터페이스.
 *
 * <p>{@link BeanCreator}가 생성자 파라미터의 의존성을 해결할 때 사용한다.
 * {@code @FunctionalInterface}이므로 {@code Container::get}과 같은 메서드 참조로 주입할 수 있다.</p>
 *
 * <p>실제 구현은 {@link dev.wonyoung.infrastructure.container.Container#get(Class)}이며,
 * 필요한 빈이 아직 없으면 그 자리에서 생성하고 등록한 뒤 반환한다.</p>
 *
 * <p>사용 예시 (Container 내부):</p>
 * <pre>{@code
 * BeanCreator creator = new BeanCreator(proxyFactory, this::get);
 * }</pre>
 */
@FunctionalInterface
public interface BeanResolver {
    /**
     * 주어진 타입의 빈 인스턴스를 반환한다.
     *
     * @param type 조회할 빈의 타입
     * @return 해당 타입의 빈 인스턴스
     * @throws Exception 빈을 찾을 수 없거나 생성에 실패한 경우
     */
    Object resolve(Class<?> type) throws Exception;
}
