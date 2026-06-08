package dev.wonyoung.dicegame.server.domain.port.in;

/**
 * 로그아웃/연결 종료 유스케이스.
 */
public interface LogoutUseCase {

    /**
     * 사용자를 로그아웃시킨다. 게임 중이었다면 상대에게도 종료를 통지하고,
     * 등록을 해제한 뒤 남은 접속자들에게 목록을 브로드캐스트한다.
     *
     * @param userId 로그아웃할 사용자 ID
     */
    void logout(String userId);
}
