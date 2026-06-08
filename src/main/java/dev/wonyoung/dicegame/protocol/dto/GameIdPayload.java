package dev.wonyoung.dicegame.protocol.dto;

/**
 * 게임 식별자 하나만 담는 payload. {@code END_GAME} 요청에서 사용한다.
 *
 * @param gameId 게임 식별자
 */
public record GameIdPayload(String gameId) {
}
