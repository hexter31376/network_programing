package dev.wonyoung.dicegame.server.domain.model;

/**
 * 한 라운드(또는 게임 전체)의 판정 결과를 특정 플레이어 관점에서 나타낸다.
 */
public enum RoundOutcome {
    WIN,
    LOSE,
    DRAW;

    /**
     * 상대 관점의 결과로 뒤집는다. {@code WIN↔LOSE}, {@code DRAW}는 그대로.
     *
     * @return 반대 관점의 결과
     */
    public RoundOutcome invert() {
        return switch (this) {
            case WIN -> LOSE;
            case LOSE -> WIN;
            case DRAW -> DRAW;
        };
    }
}
