package dev.wonyoung.dicegame.server.domain.model;

/**
 * 게임의 누적 점수를 특정 플레이어 관점에서 나타낸다.
 *
 * @param wins   이긴 라운드 수
 * @param losses 진 라운드 수
 * @param draws  무승부 라운드 수
 */
public record GameScore(int wins, int losses, int draws) {

    /**
     * 승/패 수로 최종 판정을 도출한다.
     *
     * @return 최종 결과 (이긴 라운드가 더 많으면 WIN 등)
     */
    public RoundOutcome finalOutcome() {
        if (wins > losses) {
            return RoundOutcome.WIN;
        }
        if (wins < losses) {
            return RoundOutcome.LOSE;
        }
        return RoundOutcome.DRAW;
    }
}
