package dev.wonyoung.dicegame.protocol.dto;

/**
 * {@code ERROR} 푸시 payload. 처리할 수 없는 요청에 대한 오류 통지.
 *
 * @param code    오류 코드
 * @param message 사람이 읽을 수 있는 오류 설명
 */
public record ErrorPayload(String code, String message) {
}
