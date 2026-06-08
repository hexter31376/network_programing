package dev.wonyoung.dicegame.client.domain.port.out;

import java.util.List;

/**
 * 서버 이벤트를 UI에 반영하기 위한 아웃 포트. Swing의 {@code MainFrame}이 구현한다.
 *
 * <p>{@link dev.wonyoung.dicegame.client.adapter.in.server.ServerEventHandler}가
 * 서버 푸시를 해석하여 이 콜백들을 호출한다. 구현체는 각 콜백에서 EDT로 안전하게 갱신해야 한다.</p>
 */
public interface GameEventPort {

    /**
     * 로그온 결과.
     *
     * @param success 성공 여부
     * @param reason  실패 사유
     * @param users   현재 접속자 목록
     */
    void onLoginResult(boolean success, String reason, List<String> users);

    /**
     * 접속자 목록 갱신.
     *
     * @param users 현재 접속자 목록
     */
    void onUserList(List<String> users);

    /**
     * 게임 신청을 받음.
     *
     * @param fromId 신청한 사용자 ID
     */
    void onGameRequested(String fromId);

    /**
     * 신청한 상대가 게임 중이라 신청 불가.
     *
     * @param targetId 게임 중인 대상 ID
     */
    void onGameBusy(String targetId);

    /**
     * 신청을 상대가 거절함.
     *
     * @param byId 거절한 사용자 ID
     */
    void onGameDeclined(String byId);

    /**
     * 게임 시작.
     *
     * @param gameId     게임 ID
     * @param opponentId 상대 ID
     */
    void onGameStarted(String gameId, String opponentId);

    /**
     * 라운드 판정 결과.
     *
     * @param yourSum 내 합
     * @param oppSum  상대 합
     * @param outcome 내 관점 결과 (WIN/LOSE/DRAW)
     */
    void onRoundResult(int yourSum, int oppSum, String outcome);

    /**
     * 게임 최종 결과.
     *
     * @param wins         이긴 라운드 수
     * @param losses       진 라운드 수
     * @param draws        무승부 수
     * @param finalOutcome 최종 결과
     */
    void onGameEnded(int wins, int losses, int draws, String finalOutcome);

    /**
     * 오류 통지.
     *
     * @param code    오류 코드
     * @param message 오류 메시지
     */
    void onError(String code, String message);
}
