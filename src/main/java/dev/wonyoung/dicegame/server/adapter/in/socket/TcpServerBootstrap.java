package dev.wonyoung.dicegame.server.adapter.in.socket;

import dev.wonyoung.common.container.di.Component;
import dev.wonyoung.common.container.di.Inject;
import dev.wonyoung.dicegame.protocol.codec.MessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TCP 서버 부트스트랩(인바운드 어댑터).
 *
 * <p>{@link ServerSocket}으로 접속을 받아들이고, 연결마다 {@link ClientConnectionHandler}를
 * {@link ExecutorService}(스레드 풀)에 제출한다 — thread-per-request 모델.
 * 게임 신청·라운드 결과 등 서버가 먼저 보내는 푸시는 각 연결의 notifier가 담당하므로,
 * 워커 스레드는 인바운드 읽기에만 집중한다.</p>
 */
@Component
public class TcpServerBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(TcpServerBootstrap.class);
    private static final int PORT = 5050;

    private final MessageDispatcher dispatcher;
    private final MessageCodec codec;
    private final ExecutorService workers = Executors.newCachedThreadPool();

    @Inject
    public TcpServerBootstrap(MessageDispatcher dispatcher, MessageCodec codec) {
        this.dispatcher = dispatcher;
        this.codec = codec;
    }

    /**
     * 서버를 시작한다. 접속 수락 루프는 별도 스레드에서 돌리고 이 메서드는 즉시 반환한다.
     */
    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        logger.info("주사위 게임 서버 시작 — 포트 {}", PORT);
        Thread acceptThread = new Thread(() -> acceptLoop(serverSocket), "accept-loop");
        acceptThread.start();
    }

    /**
     * 접속을 계속 받아 각 연결을 워커 풀에 넘긴다.
     */
    private void acceptLoop(ServerSocket serverSocket) {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                logger.info("클라이언트 접속: {}", socket.getRemoteSocketAddress());
                workers.submit(new ClientConnectionHandler(socket, dispatcher, codec));
            } catch (IOException e) {
                logger.error("접속 수락 실패: {}", e.getMessage());
                break;
            }
        }
    }
}
