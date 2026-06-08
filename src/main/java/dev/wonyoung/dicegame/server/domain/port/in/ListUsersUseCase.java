package dev.wonyoung.dicegame.server.domain.port.in;

/**
 * 접속자 목록 조회 유스케이스.
 */
public interface ListUsersUseCase {

    /**
     * 요청한 사용자에게 현재 접속자 목록({@code USER_LIST})을 푸시한다.
     *
     * @param userId 요청한 사용자 ID
     */
    void listUsers(String userId);
}
