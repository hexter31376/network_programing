package dev.wonyoung.dicegame.protocol.dto;

/**
 * {@code GAME_ENDED} 푸시 payload. 게임 최종 결과를 수신자 관점으로 담는다.
 *
 * @param gameId       게임 식별자
 * @param wins         내가 이긴 라운드 수
 * @param losses       내가 진 라운드 수
 * @param draws        무승부 라운드 수
 * @param finalOutcome 최종 판정 ("WIN" / "LOSE" / "DRAW")
 */
public record GameEndedPayload(String gameId, int wins, int losses, int draws, String finalOutcome) {
}
