package dev.wonyoung.dicegame.protocol.dto;

/**
 * {@code GAME_REQUESTED} 푸시 payload. 누군가 나에게 게임을 신청했음을 알린다.
 *
 * @param fromId 게임을 신청한 사용자 ID
 */
public record GameRequestedPayload(String fromId) {
}
