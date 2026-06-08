package dev.wonyoung.dicegame.protocol.dto;

/**
 * {@code LOGIN} 요청 payload.
 *
 * @param userId 로그온하려는 사용자 ID (서버에서 중복 검사 대상)
 */
public record LoginPayload(String userId) {
}
