package dev.wonyoung.dicegame.server.domain.port.in;

/**
 * 게임 신청 유스케이스.
 */
public interface RequestGameUseCase {

    /**
     * 한 사용자가 다른 사용자에게 게임을 신청한다.
     *
     * <p>대상이 게임 중이면 요청자에게 {@code GAME_BUSY}를, 존재하지 않으면 {@code ERROR}를,
     * 가능하면 대상에게 {@code GAME_REQUESTED}를 푸시한다.</p>
     *
     * @param requesterId 신청한 사용자 ID
     * @param targetId    신청 대상 사용자 ID
     */
    void requestGame(String requesterId, String targetId);
}
