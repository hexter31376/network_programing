package dev.wonyoung.dicegame.server.domain.port.in;

/**
 * 게임 종료 유스케이스.
 */
public interface EndGameUseCase {

    /**
     * 게임을 종료한다. 양측에 최종 결과({@code GAME_ENDED})를 푸시하고
     * 두 플레이어를 로비 상태로 되돌린 뒤 게임을 제거한다.
     *
     * @param playerId 종료를 요청한 플레이어 ID
     * @param gameId   게임 식별자
     */
    void endGame(String playerId, String gameId);
}
