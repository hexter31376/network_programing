package dev.wonyoung.dicegame.server.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionTest {

    @Test
    void 양쪽_제출이_모두_도착해야_라운드를_판정할_수_있다() {
        GameSession session = new GameSession("g1", "alice", "bob");

        assertFalse(session.submitRoll("alice", 10), "한쪽만 제출하면 아직 미완");
        assertTrue(session.submitRoll("bob", 7), "양쪽 제출 후 완료");
    }

    @Test
    void 합이_큰_쪽이_이기고_점수가_누적된다() {
        GameSession session = new GameSession("g1", "alice", "bob");
        session.submitRoll("alice", 10);
        session.submitRoll("bob", 7);

        RoundResult result = session.resolveRound();

        assertEquals(10, result.sumA());
        assertEquals(7, result.sumB());
        assertEquals(RoundOutcome.WIN, result.outcomeA());

        GameScore aliceScore = session.scoreOf("alice");
        assertEquals(1, aliceScore.wins());
        assertEquals(0, aliceScore.losses());
        assertEquals(RoundOutcome.WIN, aliceScore.finalOutcome());

        GameScore bobScore = session.scoreOf("bob");
        assertEquals(0, bobScore.wins());
        assertEquals(1, bobScore.losses());
        assertEquals(RoundOutcome.LOSE, bobScore.finalOutcome());
    }

    @Test
    void 같은_합이면_무승부다() {
        GameSession session = new GameSession("g1", "alice", "bob");
        session.submitRoll("alice", 8);
        session.submitRoll("bob", 8);

        RoundResult result = session.resolveRound();

        assertEquals(RoundOutcome.DRAW, result.outcomeA());
        assertEquals(1, session.scoreOf("alice").draws());
    }

    @Test
    void 판정_후_다음_라운드를_위해_초기화된다() {
        GameSession session = new GameSession("g1", "alice", "bob");
        session.submitRoll("alice", 10);
        session.submitRoll("bob", 7);
        session.resolveRound();

        assertFalse(session.submitRoll("alice", 5), "초기화되어 다시 한쪽만 제출한 상태");
    }
}
