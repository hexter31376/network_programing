package dev.wonyoung.dicegame.protocol.dto;

import java.util.List;

/**
 * {@code LOGIN_RESULT} 응답 payload.
 *
 * @param success 로그온 성공 여부 (ID 중복이면 false)
 * @param reason  실패 사유 (성공이면 빈 문자열)
 * @param users   현재 접속 중인 사용자 ID 목록
 */
public record LoginResultPayload(boolean success, String reason, List<String> users) {
}
