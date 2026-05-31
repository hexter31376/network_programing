package dev.wonyoung.server.adapter.in.socket.virtual;

import dev.wonyoung.server.adapter.in.socket.ChatServer;
import dev.wonyoung.server.application.port.in.ChatUseCase;
import dev.wonyoung.server.application.port.in.LoginUseCase;
import dev.wonyoung.server.application.port.in.LogoutUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 버추얼 스레드 기반 블로킹 서버.
 *
 * 블로킹 서버와 코드 구조는 동일하지만 두 가지가 다르다:
 *   1. accept 루프 자체를 버추얼 스레드에서 실행
 *   2. 클라이언트 핸들러를 newVirtualThreadPerTaskExecutor()로 실행
 *      → 플랫폼 스레드 풀 크기 제한 없이 클라이언트마다 경량 스레드 할당
 */
public class VirtualServer implements ChatServer {

    private static final Logger logger = LoggerFactory.getLogger(VirtualServer.class);
    private static final int PORT = 8080;

    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ChatUseCase chatUseCase;

    private ServerSocket serverSocket;
    private Thread acceptThread;
    private ExecutorService handlerExecutor;
    private volatile boolean running = false;

    public VirtualServer(LoginUseCase loginUseCase, LogoutUseCase logoutUseCase, ChatUseCase chatUseCase) {
        this.loginUseCase = loginUseCase;
        this.logoutUseCase = logoutUseCase;
        this.chatUseCase = chatUseCase;
    }

    @Override
    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        // 클라이언트 핸들러: 연결마다 버추얼 스레드 1개
        handlerExecutor = Executors.newVirtualThreadPerTaskExecutor();
        running = true;
        // accept 루프도 버추얼 스레드에서 실행
        acceptThread = Thread.ofVirtual().name("accept-loop").start(this::acceptLoop);
        logger.info("서버 시작 (Virtual): 포트 {}", PORT);
    }

    @Override
    public void stop() {
        running = false;
        logoutUseCase.shutdownAll();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException e) {
            logger.error("서버 소켓 종료 중 오류", e);
        }
        if (handlerExecutor != null) handlerExecutor.shutdown();
        logger.info("서버 종료 (Virtual)");
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                logger.info("연결: {}", socket.getRemoteSocketAddress());
                handlerExecutor.submit(
                        new VirtualClientHandler(socket, loginUseCase, logoutUseCase, chatUseCase)
                );
            } catch (IOException e) {
                if (running) logger.error("연결 수락 오류", e);
            }
        }
    }
}
