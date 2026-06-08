package dev.wonyoung.dicegame.protocol.dto;

/**
 * {@code GAME_STARTED} 푸시 payload. 게임이 시작되었음을 양측에 알린다.
 *
 * @param gameId     게임 식별자
 * @param opponentId 상대 사용자 ID
 */
public record GameStartedPayload(String gameId, String opponentId) {
}
