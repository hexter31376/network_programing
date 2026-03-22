package dev.wonyoung.infrastructure.container.di;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 이 어노테이션이 붙은 클래스는 컨테이너가 관리하는 빈(Bean)의 후보로 등록된다.
 *
 * <p>{@link dev.wonyoung.infrastructure.container.scanner.ClassScanner}가 패키지를 스캔할 때
 * 이 어노테이션의 존재 여부를 확인하여 후보 클래스 목록에 추가한다.
 * 이후 {@link dev.wonyoung.infrastructure.container.Container#get(Class)}이 호출되면
 * 그 시점에 인스턴스가 생성되고 컨테이너에 등록된다(지연 초기화).</p>
 *
 * <p>사용 예시:</p>
 * <pre>{@code
 * @Component
 * public class UserService {
 *     // 컨테이너가 이 클래스를 빈으로 관리한다
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {
}
