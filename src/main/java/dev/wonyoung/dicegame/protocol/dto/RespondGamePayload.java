package dev.wonyoung.dicegame.protocol.dto;

/**
 * {@code RESPOND_GAME} 요청 payload. 받은 게임 신청에 대한 응답.
 *
 * @param requesterId 게임을 신청한 사용자 ID
 * @param accept      수락 여부 (true=수락, false=거절)
 */
public record RespondGamePayload(String requesterId, boolean accept) {
}
