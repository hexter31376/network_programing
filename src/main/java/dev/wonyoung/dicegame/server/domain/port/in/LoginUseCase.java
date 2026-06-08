package dev.wonyoung.dicegame.server.domain.port.in;

import dev.wonyoung.dicegame.server.domain.port.out.ClientNotifier;

/**
 * 로그온 유스케이스. ID 중복을 검사하고 결과를 클라이언트에 통지한다.
 */
public interface LoginUseCase {

    /**
     * 사용자를 로그온시킨다.
     *
     * <p>성공/실패와 무관하게 요청 클라이언트에 {@code LOGIN_RESULT}를 푸시하고,
     * 성공 시 다른 접속자들에게 {@code USER_LIST}를 브로드캐스트한다.</p>
     *
     * @param userId   로그온하려는 ID
     * @param notifier 이 연결의 푸시 채널
     * @return 성공 시 로그온된 사용자 ID, ID 중복이면 {@code null}
     */
    String login(String userId, ClientNotifier notifier);
}
