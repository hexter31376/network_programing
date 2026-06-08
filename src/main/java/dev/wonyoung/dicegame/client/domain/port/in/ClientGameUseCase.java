package dev.wonyoung.dicegame.client.domain.port.in;

/**
 * 클라이언트 측 유스케이스 퍼사드. Swing UI가 사용자 행동을 이 인터페이스로 전달한다.
 *
 * <p>게임 식별자 같은 진행 상태는 UI가 보유하고 메서드 인자로 넘긴다(서비스는 무상태).
 * 서버의 응답·푸시는 이 인터페이스가 아니라
 * {@link dev.wonyoung.dicegame.client.domain.port.out.GameEventPort}로 전달된다.</p>
 */
public interface ClientGameUseCase {

    /**
     * 서버에 접속한다.
     *
     * @param host 서버 호스트
     * @param port 서버 포트
     * @return 접속 성공 여부
     */
    boolean connect(String host, int port);

    /**
     * 로그온을 요청한다.
     *
     * @param userId 사용할 ID
     */
    void login(String userId);

    /** 접속자 목록 갱신을 요청한다. */
    void refreshUsers();

    /**
     * 상대에게 게임을 신청한다.
     *
     * @param targetId 신청 대상 ID
     */
    void requestGame(String targetId);

    /**
     * 받은 게임 신청에 응답한다.
     *
     * @param requesterId 신청한 사용자 ID
     * @param accept      수락 여부
     */
    void respondGame(String requesterId, boolean accept);

    /**
     * 주사위 2개를 굴려 결과를 서버로 보낸다.
     *
     * @param gameId 진행 중인 게임 ID
     * @return 굴린 주사위 2개의 눈 (UI 표시용)
     */
    int[] rollDice(String gameId);

    /**
     * 게임을 종료한다.
     *
     * @param gameId 진행 중인 게임 ID
     */
    void endGame(String gameId);

    /** 로그아웃한다(초기화면 복귀). */
    void logout();

    /** 서버와의 연결을 끊는다. */
    void disconnect();
}
