package dev.wonyoung.dicegame.protocol.dto;

import java.util.List;

/**
 * {@code USER_LIST} 푸시 payload. 접속자 목록이 바뀔 때 브로드캐스트된다.
 *
 * @param users 현재 접속 중인 사용자 ID 목록
 */
public record UserListPayload(List<String> users) {
}
