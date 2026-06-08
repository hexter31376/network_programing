package dev.wonyoung.itest;

import dev.wonyoung.common.container.Container;
import dev.wonyoung.common.exception.ExceptionHandler;
import dev.wonyoung.dicegame.client.adapter.in.server.ServerEventHandler;
import dev.wonyoung.dicegame.client.domain.port.in.ClientGameUseCase;
import dev.wonyoung.dicegame.client.domain.port.out.GameEventPort;
import dev.wonyoung.dicegame.client.domain.port.out.ServerGateway;
import dev.wonyoung.dicegame.protocol.codec.GsonMessageCodec;
import dev.wonyoung.dicegame.protocol.codec.MessageCodec;
import dev.wonyoung.dicegame.server.adapter.in.socket.MessageDispatcher;
import dev.wonyoung.dicegame.server.adapter.in.socket.TcpServerBootstrap;
import dev.wonyoung.dicegame.server.adapter.out.InMemoryGameRepository;
import dev.wonyoung.dicegame.server.adapter.out.InMemoryPlayerRegistry;
import dev.wonyoung.dicegame.server.domain.service.GameService;
import dev.wonyoung.dicegame.server.domain.service.LobbyService;
import dev.wonyoung.dicegame.server.domain.service.LoginService;
import dev.wonyoung.dicegame.server.domain.service.MatchmakingService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 클라이언트의 프록시된 유스케이스 경로(ClientGameService -> TcpServerGateway)가
 * 실제 TCP로 서버와 정상 동작하는지 검증한다. GUI "게임 신청" 버튼이 호출하는 바로 그 경로다.
 *
 * <p>서버는 수동 배선한다(서버 컨테이너 스캔은 테스트 출력 디렉터리 충돌로 빈을 못 찾기 때문).
 * 클라이언트는 실제 컨테이너 + ByteBuddy 프록시 + ExceptionHandler 경로를 그대로 사용한다.</p>
 */
class ClientServerTcpIntegrationTest {

    /** GUI 대신 서버 이벤트를 받아 기록하는 테스트용 포트. */
    private static final class CapturePort implements GameEventPort {
        volatile Boolean loginSuccess;
        volatile String gameRequestedFrom;
        final List<String> users = new CopyOnWriteArrayList<>();

        public void onLoginResult(boolean success, String reason, List<String> users) {
            this.loginSuccess = success;
        }

        public void onUserList(List<String> users) {
            this.users.clear();
            this.users.addAll(users);
        }

        public void onGameRequested(String fromId) {
            this.gameRequestedFrom = fromId;
        }

        public void onGameBusy(String targetId) {
        }

        public void onGameDeclined(String byId) {
        }

        public void onGameStarted(String gameId, String opponentId) {
        }

        public void onRoundResult(int yourSum, int oppSum, String outcome) {
        }

        public void onGameEnded(int wins, int losses, int draws, String finalOutcome) {
        }

        public void onError(String code, String message) {
        }
    }

    @BeforeAll
    static void startServer() throws Exception {
        MessageCodec codec = new GsonMessageCodec();
        InMemoryPlayerRegistry registry = new InMemoryPlayerRegistry();
        InMemoryGameRepository games = new InMemoryGameRepository();
        LoginService login = new LoginService(registry, codec);
        LobbyService lobby = new LobbyService(registry, games, codec);
        MatchmakingService match = new MatchmakingService(registry, games, codec);
        GameService game = new GameService(registry, games, codec);
        MessageDispatcher dispatcher = new MessageDispatcher(login, lobby, lobby, match, match, game, game, codec);
        new TcpServerBootstrap(dispatcher, codec).start();
        Thread.sleep(300);
    }

    private ClientGameUseCase newClient(GameEventPort port) throws Exception {
        Container c = new Container("dev.wonyoung.dicegame.client");
        c.addInterceptor(new ExceptionHandler());
        c.register(MessageCodec.class, new GsonMessageCodec());
        ServerGateway gateway = c.get(ServerGateway.class);
        ServerEventHandler eventHandler = c.get(ServerEventHandler.class);
        ClientGameUseCase useCase = c.get(ClientGameUseCase.class);
        eventHandler.bindEventPort(port);
        gateway.setMessageListener(eventHandler::handle);
        return useCase;
    }

    private void waitUntil(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(20);
        }
    }

    @Test
    void 게임_신청이_프록시_경로를_거쳐_상대에게_전달된다() throws Exception {
        CapturePort aliceP = new CapturePort();
        CapturePort bobP = new CapturePort();
        ClientGameUseCase alice = newClient(aliceP);
        ClientGameUseCase bob = newClient(bobP);

        assertTrue(alice.connect("localhost", 5050), "alice 접속");
        assertTrue(bob.connect("localhost", 5050), "bob 접속");

        alice.login("alice");
        bob.login("bob");
        waitUntil(() -> Boolean.TRUE.equals(aliceP.loginSuccess));
        waitUntil(() -> Boolean.TRUE.equals(bobP.loginSuccess));
        assertTrue(aliceP.loginSuccess);
        assertTrue(bobP.loginSuccess);

        // GUI의 "게임 신청" 버튼이 호출하는 바로 그 메서드
        alice.requestGame("bob");
        waitUntil(() -> bobP.gameRequestedFrom != null);

        assertEquals("alice", bobP.gameRequestedFrom, "bob이 alice의 신청을 받아야 한다");

        alice.disconnect();
        bob.disconnect();
    }
}
