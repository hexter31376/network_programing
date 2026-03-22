package dev.wonyoung.infrastructure.container.di;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 이 어노테이션이 붙은 생성자를 통해 의존성을 주입받겠다는 것을 선언한다.
 *
 * <p>{@link dev.wonyoung.infrastructure.container.factory.BeanCreator}가 인스턴스를 생성할 때
 * 이 어노테이션이 붙은 생성자를 우선적으로 찾는다.
 * 생성자의 각 파라미터 타입은 {@link dev.wonyoung.infrastructure.container.factory.BeanResolver}를 통해
 * 컨테이너로부터 자동으로 조회·주입된다.</p>
 *
 * <p>이 어노테이션이 붙은 생성자가 없으면 기본 생성자(no-arg constructor)를 사용한다.</p>
 *
 * <p>사용 예시:</p>
 * <pre>{@code
 * @Component
 * public class OrderService {
 *     private final UserService userService;
 *
 *     @Inject
 *     public OrderService(UserService userService) {
 *         // 컨테이너가 UserService 빈을 찾아서 이 생성자에 넘겨준다
 *         this.userService = userService;
 *     }
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface Inject {
}
