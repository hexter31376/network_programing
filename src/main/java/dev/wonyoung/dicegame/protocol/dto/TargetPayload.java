package dev.wonyoung.dicegame.protocol.dto;

/**
 * 상대 사용자 ID 하나만 담는 공용 payload.
 *
 * <p>{@code REQUEST_GAME}(신청 대상), {@code GAME_BUSY}(게임 중인 대상),
 * {@code GAME_DECLINED}(거절한 사용자)에서 재사용한다.</p>
 *
 * @param targetId 대상 사용자 ID
 */
public record TargetPayload(String targetId) {
}
