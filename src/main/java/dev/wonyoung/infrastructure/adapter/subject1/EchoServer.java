package dev.wonyoung.infrastructure.adapter.subject1;

import dev.wonyoung.domin.port.in.EchoUseCase;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Component
public class EchoServer {

    private static final Logger log = Logger.getLogger(EchoServer.class.getName());
    private static final int PORT = 7;
    private static final int CORE_POOL = Runtime.getRuntime().availableProcessors() * 2;
    private static final int MAX_POOL = 200;
    private static final long KEEP_ALIVE = 60L;

    private final EchoUseCase echoUseCase;

    @Inject
    public EchoServer(EchoUseCase echoUseCase) {
        this.echoUseCase = echoUseCase;
    }

    public void start() {
        var executor = new ThreadPoolExecutor(
            CORE_POOL, MAX_POOL,
            KEEP_ALIVE, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );

        try (var serverSocket = new ServerSocket(PORT)) {
            log.info("ThreadPool Echo Server listening on port " + PORT);
            while (!serverSocket.isClosed()) {
                var clientSocket = serverSocket.accept();
                executor.submit(new ClientHandler(clientSocket, echoUseCase));
            }
        } catch (IOException e) {
            log.warning("Server error: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}
