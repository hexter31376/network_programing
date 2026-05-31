package dev.wonyoung.server.adapter.in.socket.blocking;

import dev.wonyoung.server.application.port.in.ChatUseCase;
import dev.wonyoung.server.application.port.in.LoginUseCase;
import dev.wonyoung.server.application.port.in.LogoutUseCase;
import dev.wonyoung.server.adapter.in.socket.ChatServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 스레드 per 클라이언트 방식의 블로킹 소켓 서버다.
 *
 * ServerSocket.accept()가 블로킹 상태로 연결을 기다리고,
 * 연결이 들어오면 스레드 풀에서 ClientHandler를 꺼내 처리를 위임한다.
 * 코드가 단순하고 직관적이지만 동시 접속자 수만큼 스레드가 필요해 대규모 환경에는 적합하지 않다.
 * accept 루프는 별도의 단일 스레드 ExecutorService에서 실행해 메인 스레드를 블로킹하지 않는다.
 */
public class BlockingServer implements ChatServer {

    private static final Logger logger = LoggerFactory.getLogger(BlockingServer.class);
    private static final int PORT = 8080;
    private static final int THREAD_POOL_SIZE = 10;

    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ChatUseCase chatUseCase;

    private ServerSocket serverSocket;
    private ExecutorService acceptExecutor;
    private ExecutorService handlerPool;
    private volatile boolean running = false;

    public BlockingServer(LoginUseCase loginUseCase, LogoutUseCase logoutUseCase, ChatUseCase chatUseCase) {
        this.loginUseCase = loginUseCase;
        this.logoutUseCase = logoutUseCase;
        this.chatUseCase = chatUseCase;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        acceptExecutor = Executors.newSingleThreadExecutor();
        handlerPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        running = true;
        acceptExecutor.submit(this::acceptLoop);
        logger.info("서버 시작: 포트 {}", PORT);
    }

    public void stop() {
        running = false;
        logoutUseCase.shutdownAll();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException e) {
            logger.error("서버 소켓 종료 중 오류", e);
        }
        if (acceptExecutor != null) acceptExecutor.shutdown();
        if (handlerPool != null) handlerPool.shutdown();
        logger.info("서버 종료");
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                logger.info("연결: {}", socket.getRemoteSocketAddress());
                handlerPool.submit(new ClientHandler(socket, loginUseCase, logoutUseCase, chatUseCase));
            } catch (IOException e) {
                if (running) logger.error("연결 수락 오류", e);
            }
        }
    }
}
