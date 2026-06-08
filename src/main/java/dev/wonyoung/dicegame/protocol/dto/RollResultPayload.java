package dev.wonyoung.dicegame.protocol.dto;

/**
 * {@code ROLL_RESULT} 요청 payload. 클라이언트가 굴린 주사위 2개와 그 합.
 *
 * @param gameId 게임 식별자
 * @param dice   주사위 2개의 눈 (길이 2 배열)
 * @param sum    두 주사위 눈의 합 (서버가 비교에 사용)
 */
public record RollResultPayload(String gameId, int[] dice, int sum) {
}
