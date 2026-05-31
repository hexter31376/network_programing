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
