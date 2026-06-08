package dev.wonyoung.dicegame.protocol.dto;

/**
 * {@code ROUND_RESULT} 푸시 payload. 한 라운드 판정 결과를 수신자 관점으로 담는다.
 *
 * @param gameId  게임 식별자
 * @param yourSum 내 주사위 합
 * @param oppSum  상대 주사위 합
 * @param outcome 내 관점 판정 결과 ("WIN" / "LOSE" / "DRAW")
 */
public record RoundResultPayload(String gameId, int yourSum, int oppSum, String outcome) {
}
