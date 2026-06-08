package dev.wonyoung.dicegame.server.domain.service;

import dev.wonyoung.dicegame.protocol.Message;
import dev.wonyoung.dicegame.protocol.MessageType;
import dev.wonyoung.dicegame.protocol.codec.GsonMessageCodec;
import dev.wonyoung.dicegame.protocol.codec.MessageCodec;
import dev.wonyoung.dicegame.protocol.dto.GameEndedPayload;
import dev.wonyoung.dicegame.protocol.dto.GameStartedPayload;
import dev.wonyoung.dicegame.protocol.dto.LoginResultPayload;
import dev.wonyoung.dicegame.protocol.dto.RoundResultPayload;
import dev.wonyoung.dicegame.server.adapter.out.InMemoryGameRepository;
import dev.wonyoung.dicegame.server.adapter.out.InMemoryPlayerRegistry;
import dev.wonyoung.dicegame.server.domain.port.out.ClientNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 소켓 없이 인메모리 어댑터로 서비스 계층 전체 게임 흐름을 검증한다.
 * (로그인 -> 신청 -> 수락 -> 주사위 -> 라운드 판정 -> 종료)
 */
class GameFlowIntegrationTest {

    /** 푸시된 메시지를 모아두는 테스트용 notifier. */
    private static final class FakeNotifier implements ClientNotifier {
        final List<Message> received = new ArrayList<>();

        @Override
        public void push(Message message) {
            received.add(message);
        }

        Message last(MessageType type) {
            for (int i = received.size() - 1; i >= 0; i--) {
                if (received.get(i).type() == type) {
                    return received.get(i);
                }
            }
            return null;
        }
    }

    private final MessageCodec codec = new GsonMessageCodec();
    private InMemoryPlayerRegistry registry;
    private InMemoryGameRepository games;
    private LoginService loginService;
    private MatchmakingService matchmaking;
    private GameService gameService;

    private FakeNotifier aliceChannel;
    private FakeNotifier bobChannel;

    @BeforeEach
    void setUp() {
        registry = new InMemoryPlayerRegistry();
        games = new InMemoryGameRepository();
        loginService = new LoginService(registry, codec);
        matchmaking = new MatchmakingService(registry, games, codec);
        gameService = new GameService(registry, games, codec);
        aliceChannel = new FakeNotifier();
        bobChannel = new FakeNotifier();
    }

    @Test
    void 전체_게임_흐름이_정상_동작한다() {
        // 로그온
        assertEquals("alice", loginService.login("alice", aliceChannel));
        assertEquals("bob", loginService.login("bob", bobChannel));

        LoginResultPayload aliceLogin = codec.payloadAs(aliceChannel.last(MessageType.LOGIN_RESULT), LoginResultPayload.class);
        assertTrue(aliceLogin.success());

        // 신청 -> 수락
        matchmaking.requestGame("alice", "bob");
        assertNotNull(bobChannel.last(MessageType.GAME_REQUESTED), "bob이 신청을 받아야 한다");

        matchmaking.respondGame("bob", "alice", true);
        GameStartedPayload started = codec.payloadAs(aliceChannel.last(MessageType.GAME_STARTED), GameStartedPayload.class);
        assertEquals("bob", started.opponentId());
        String gameId = started.gameId();

        // 주사위 제출 (alice 10 > bob 7)
        gameService.submitRoll("alice", gameId, 10);
        gameService.submitRoll("bob", gameId, 7);

        RoundResultPayload aliceRound = codec.payloadAs(aliceChannel.last(MessageType.ROUND_RESULT), RoundResultPayload.class);
        assertEquals(10, aliceRound.yourSum());
        assertEquals(7, aliceRound.oppSum());
        assertEquals("WIN", aliceRound.outcome());

        RoundResultPayload bobRound = codec.payloadAs(bobChannel.last(MessageType.ROUND_RESULT), RoundResultPayload.class);
        assertEquals("LOSE", bobRound.outcome());

        // 종료
        gameService.endGame("alice", gameId);
        GameEndedPayload aliceEnded = codec.payloadAs(aliceChannel.last(MessageType.GAME_ENDED), GameEndedPayload.class);
        assertEquals(1, aliceEnded.wins());
        assertEquals("WIN", aliceEnded.finalOutcome());
        assertTrue(registry.find("alice").isInLobby(), "종료 후 로비로 복귀");
    }

    @Test
    void 중복_ID는_로그온에_실패한다() {
        assertEquals("alice", loginService.login("alice", aliceChannel));
        assertEquals(null, loginService.login("alice", bobChannel));

        LoginResultPayload dup = codec.payloadAs(bobChannel.last(MessageType.LOGIN_RESULT), LoginResultPayload.class);
        assertTrue(!dup.success());
    }

    @Test
    void 게임_중인_상대에게_신청하면_BUSY를_받는다() {
        loginService.login("alice", aliceChannel);
        loginService.login("bob", bobChannel);
        matchmaking.requestGame("alice", "bob");
        matchmaking.respondGame("bob", "alice", true);

        FakeNotifier carolChannel = new FakeNotifier();
        loginService.login("carol", carolChannel);
        matchmaking.requestGame("carol", "alice");

        assertNotNull(carolChannel.last(MessageType.GAME_BUSY), "게임 중인 alice 신청 시 BUSY");
    }
}
