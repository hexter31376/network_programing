package dev.wonyoung.dicegame.server.domain.model;

/**
 * 두 플레이어 간 진행 중인 게임 한 판.
 *
 * <p>각 라운드마다 두 플레이어의 주사위 합을 모은다. 둘 다 도착하면
 * {@link #resolveRound()}로 비교·집계하고 다음 라운드를 위해 초기화한다.</p>
 *
 * <h2>동시성</h2>
 * <p>두 플레이어의 {@code ROLL_RESULT}가 서로 다른 워커 스레드에서 동시에 도착할 수 있으므로
 * roll 수집·판정 메서드는 모두 {@code synchronized}로 보호한다.</p>
 */
public class GameSession {

    private final String gameId;
    private final String playerA;
    private final String playerB;

    private Integer rollA;
    private Integer rollB;

    private int winsA;
    private int winsB;
    private int draws;

    public GameSession(String gameId, String playerA, String playerB) {
        this.gameId = gameId;
        this.playerA = playerA;
        this.playerB = playerB;
    }

    public String getGameId() {
        return gameId;
    }

    public String getPlayerA() {
        return playerA;
    }

    public String getPlayerB() {
        return playerB;
    }

    public boolean isParticipant(String playerId) {
        return playerA.equals(playerId) || playerB.equals(playerId);
    }

    public String opponentOf(String playerId) {
        return playerA.equals(playerId) ? playerB : playerA;
    }

    /**
     * 한 플레이어의 이번 라운드 주사위 합을 기록한다.
     *
     * @param playerId 제출한 플레이어
     * @param sum      주사위 2개의 합
     * @return 두 플레이어 모두 제출하여 라운드를 판정할 수 있으면 {@code true}
     */
    public synchronized boolean submitRoll(String playerId, int sum) {
        if (playerA.equals(playerId)) {
            rollA = sum;
        } else if (playerB.equals(playerId)) {
            rollB = sum;
        }
        return rollA != null && rollB != null;
    }

    /**
     * 모인 두 합을 비교하여 라운드 결과를 만들고 점수를 누적한 뒤, 다음 라운드를 위해 초기화한다.
     *
     * @return playerA 관점의 라운드 결과
     */
    public synchronized RoundResult resolveRound() {
        int a = rollA;
        int b = rollB;
        RoundOutcome outcomeA;
        if (a > b) {
            winsA++;
            outcomeA = RoundOutcome.WIN;
        } else if (a < b) {
            winsB++;
            outcomeA = RoundOutcome.LOSE;
        } else {
            draws++;
            outcomeA = RoundOutcome.DRAW;
        }
        rollA = null;
        rollB = null;
        return new RoundResult(a, b, outcomeA);
    }

    /**
     * 지정한 플레이어 관점의 누적 점수를 반환한다.
     *
     * @param playerId 기준 플레이어
     * @return 해당 플레이어 관점의 {@link GameScore}
     */
    public synchronized GameScore scoreOf(String playerId) {
        if (playerA.equals(playerId)) {
            return new GameScore(winsA, winsB, draws);
        }
        return new GameScore(winsB, winsA, draws);
    }
}
