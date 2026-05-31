package dev.wonyoung.server.adapter.in.socket.async;

import dev.wonyoung.server.adapter.in.socket.ChatServer;
import dev.wonyoung.server.application.port.in.ChatUseCase;
import dev.wonyoung.server.application.port.in.LoginUseCase;
import dev.wonyoung.server.application.port.in.LogoutUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ForkJoinPool;

public class AsyncServer implements ChatServer {

    private static final Logger logger = LoggerFactory.getLogger(AsyncServer.class);
    private static final int PORT = 8080;

    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ChatUseCase chatUseCase;

    private AsynchronousServerSocketChannel serverChannel;
    private AsynchronousChannelGroup channelGroup;
    private volatile boolean running = false;

    public AsyncServer(LoginUseCase loginUseCase, LogoutUseCase logoutUseCase, ChatUseCase chatUseCase) {
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
        acceptNext();
        logger.info("서버 시작 (Async, ForkJoinPool parallelism={}): 포트 {}", pool.getParallelism(), PORT);
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
        logger.info("서버 종료 (Async)");
    }

    private void acceptNext() {
        serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel client, Void a) {
                if (running) acceptNext();

                AsyncSession session = new AsyncSession(client);
                String[] nickname = {null};
                boolean[] loggedIn = {false};
                ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();
                ByteBuffer buf = ByteBuffer.allocate(1024);

                client.read(buf, null, new CompletionHandler<Integer, Void>() {
                    @Override
                    public void completed(Integer bytesRead, Void a) {
                        if (bytesRead == -1) {
                            if (loggedIn[0]) logoutUseCase.logout(nickname[0]);
                            try { client.close(); } catch (IOException ignored) {}
                            return;
                        }
                        buf.flip();
                        while (buf.hasRemaining()) {
                            byte b = buf.get();
                            if (b == '\n') {
                                String line = lineBuffer.toString(StandardCharsets.UTF_8).trim();
                                lineBuffer.reset();
                                if (!line.isBlank()) {
                                    if (!loggedIn[0]) {
                                        if (!loginUseCase.login(session, line)) {
                                            try { session.send("이미 사용 중인 닉네임입니다: " + line); } catch (IOException ignored) {}
                                            try { client.close(); } catch (IOException ignored) {}
                                            return;
                                        }
                                        nickname[0] = line;
                                        loggedIn[0] = true;
                                    } else if (line.startsWith("/w ")) {
                                        String[] parts = line.substring(3).split(" ", 2);
                                        if (parts.length < 2) {
                                            try { session.send("사용법: /w 닉네임 메시지"); } catch (IOException ignored) {}
                                        } else {
                                            chatUseCase.sendOneClient(nickname[0], parts[1], parts[0]);
                                        }
                                    } else {
                                        chatUseCase.sendAllClients(nickname[0], line);
                                    }
                                }
                            } else if (b != '\r') {
                                lineBuffer.write(b);
                            }
                        }
                        buf.clear();
                        client.read(buf, null, this);
                    }

                    @Override
                    public void failed(Throwable exc, Void a) {
                        if (!(exc instanceof AsynchronousCloseException)) {
                            logger.error("읽기 실패", exc);
                        }
                        if (loggedIn[0]) logoutUseCase.logout(nickname[0]);
                        try { client.close(); } catch (IOException ignored) {}
                    }
                });
            }

            @Override
            public void failed(Throwable exc, Void a) {
                if (!(exc instanceof AsynchronousCloseException)) {
                    logger.error("accept 실패", exc);
                }
            }
        });
    }
}