package dev.wonyoung.server.adapter.in.socket.asyncv2;

import dev.wonyoung.server.adapter.in.socket.ChatServer;
import dev.wonyoung.server.application.port.in.ChatUseCase;
import dev.wonyoung.server.application.port.in.LoginUseCase;
import dev.wonyoung.server.application.port.in.LogoutUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

/**
 * NIO2 비동기 소켓 서버다. CompletableFuture 체이닝 방식을 사용한다.
 *
 * AsyncServer와 동일하게 ForkJoinPool을 AsynchronousChannelGroup에 등록한다.
 * CompletionHandler를 CompletableFuture로 래핑해 콜백 중첩을 없애고
 * thenAcceptAsync로 체이닝하므로 콜백 완료 통지 스레드가 즉시 반환된다.
 * readLoop는 Runnable 배열 트릭을 사용해 람다 안에서 자기 자신을 재귀 호출한다.
 * 세션별 로그인 및 메시지 처리 로직은 AsyncSessionV2에 위임한다.
 */
public class AsyncServerV2 implements ChatServer {

    private static final Logger logger = LoggerFactory.getLogger(AsyncServerV2.class);
    private static final int PORT = 8080;

    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ChatUseCase chatUseCase;

    private AsynchronousServerSocketChannel serverChannel;
    private AsynchronousChannelGroup channelGroup;
    private volatile boolean running = false;

    public AsyncServerV2(LoginUseCase loginUseCase, LogoutUseCase logoutUseCase, ChatUseCase chatUseCase) {
        this.loginUseCase = loginUseCase;
        this.logoutUseCase = logoutUseCase;
        this.chatUseCase = chatUseCase;
    }

    @Override
    public void start() throws IOException {
        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        channelGroup = AsynchronousChannelGroup.withThreadPool(pool);
        serverChannel = AsynchronousServerSocketChannel.open(channelGroup);
        serverChannel.bind(new InetSocketAddress(PORT));
        running = true;
        acceptLoop();
        logger.info("서버 시작 (AsyncV2, ForkJoinPool parallelism={}): 포트 {}", pool.getParallelism(), PORT);
    }

    @Override
    public void stop() {
        running = false;
        logoutUseCase.shutdownAll();
        try {
            if (serverChannel != null) serverChannel.close();
            if (channelGroup != null) channelGroup.shutdownNow();
        } catch (IOException e) {
            logger.error("서버 종료 중 오류", e);
        }
        logger.info("서버 종료 (AsyncV2)");
    }

    private void acceptLoop() {
        CompletableFuture<AsynchronousSocketChannel> acceptCf = new CompletableFuture<>();
        serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            public void completed(AsynchronousSocketChannel r, Void a) { acceptCf.complete(r); }
            public void failed(Throwable e, Void a) { acceptCf.completeExceptionally(e); }
        });

        acceptCf
            .thenAcceptAsync(client -> {
                if (running) acceptLoop();
                try { logger.info("연결: {}", client.getRemoteAddress()); } catch (IOException ignored) {}
                AsyncSessionV2 session = new AsyncSessionV2(client, loginUseCase, logoutUseCase, chatUseCase);

                ByteBuffer buffer = ByteBuffer.allocate(1024);
                Runnable[] readLoop = new Runnable[1];
                readLoop[0] = () -> {
                    CompletableFuture<Integer> readCf = new CompletableFuture<>();
                    client.read(buffer, null, new CompletionHandler<Integer, Void>() {
                        public void completed(Integer r, Void a) { readCf.complete(r); }
                        public void failed(Throwable e, Void a) { readCf.completeExceptionally(e); }
                    });
                    readCf
                        .thenAcceptAsync(bytesRead -> {
                            if (bytesRead == -1) { session.close(); return; }
                            buffer.flip();
                            session.processBuffer(buffer);
                            buffer.clear();
                            readLoop[0].run();
                        })
                        .exceptionally(exc -> {
                            if (!(exc.getCause() instanceof AsynchronousCloseException)) {
                                logger.error("읽기 실패", exc);
                            }
                            session.close();
                            return null;
                        });
                };
                readLoop[0].run();
            })
            .exceptionally(exc -> {
                if (running && !(exc.getCause() instanceof AsynchronousCloseException)) {
                    logger.error("accept 실패", exc);
                }
                return null;
            });
    }
}
