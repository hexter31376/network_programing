package dev.wonyoung.dicegame.server.domain.port.in;

/**
 * 게임 신청 응답(수락/거절) 유스케이스.
 */
public interface RespondGameUseCase {

    /**
     * 받은 게임 신청에 응답한다.
     *
     * <p>수락하면 게임을 생성하고 양측에 {@code GAME_STARTED}를 푸시한다.
     * 거절하면 신청자에게 {@code GAME_DECLINED}를 푸시한다.</p>
     *
     * @param responderId 응답한 사용자 ID
     * @param requesterId 신청했던 사용자 ID
     * @param accept      수락 여부
     */
    void respondGame(String responderId, String requesterId, boolean accept);
}
