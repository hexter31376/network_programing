package dev.wonyoung.dicegame.server.domain.model;

/**
 * 한 라운드의 비교 결과. 항상 {@code playerA} 관점으로 기록한다.
 *
 * @param sumA     playerA의 주사위 합
 * @param sumB     playerB의 주사위 합
 * @param outcomeA playerA 관점의 판정 결과
 */
public record RoundResult(int sumA, int sumB, RoundOutcome outcomeA) {
}
