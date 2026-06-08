package dev.wonyoung.dicegame.server.domain.port.in;

/**
 * 주사위 결과 제출 유스케이스.
 */
public interface SubmitRollUseCase {

    /**
     * 한 플레이어가 이번 라운드 주사위 합을 제출한다.
     *
     * <p>두 플레이어 모두 제출하면 비교하여 양측에 {@code ROUND_RESULT}를 푸시한다.</p>
     *
     * @param playerId 제출한 플레이어 ID
     * @param gameId   게임 식별자
     * @param sum      주사위 2개의 합
     */
    void submitRoll(String playerId, String gameId, int sum);
}
