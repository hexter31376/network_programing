package dev.wonyoung.infrastructure.adapter.subject2;

import dev.wonyoung.domin.port.in.ServeFileUseCase;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Component
public class FileServer {

    private static final Logger log = Logger.getLogger(FileServer.class.getName());
    private static final int CORE_POOL = Runtime.getRuntime().availableProcessors() * 2;
    private static final int MAX_POOL = 200;
    private static final long KEEP_ALIVE = 60L;

    private final FileServerConfig config;
    private final ServeFileUseCase serveFileUseCase;

    @Inject
    public FileServer(FileServerConfig config, ServeFileUseCase serveFileUseCase) {
        this.config = config;
        this.serveFileUseCase = serveFileUseCase;
    }

    public void start() {
        var executor = new ThreadPoolExecutor(
            CORE_POOL, MAX_POOL,
            KEEP_ALIVE, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(500),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );

        try (var serverSocket = new ServerSocket(config.port())) {
            log.info("Text File Server started");
            log.info("Root : " + config.rootDirectory());
            log.info("URL  : http://localhost:" + config.port());

            while (!serverSocket.isClosed()) {
                var client = serverSocket.accept();
                executor.submit(new RequestHandler(client, serveFileUseCase));
            }
        } catch (IOException e) {
            log.warning("Server error: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}
