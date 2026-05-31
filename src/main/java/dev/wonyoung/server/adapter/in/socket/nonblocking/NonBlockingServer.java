package dev.wonyoung.server.adapter.in.socket.nonblocking;

import dev.wonyoung.server.adapter.in.socket.ChatServer;
import dev.wonyoung.server.application.port.in.ChatUseCase;
import dev.wonyoung.server.application.port.in.LoginUseCase;
import dev.wonyoung.server.application.port.in.LogoutUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * NIO Selector 기반의 논블로킹 소켓 서버다.
 *
 * 단일 스레드에서 Selector로 여러 채널의 이벤트를 감시한다.
 * OP_ACCEPT 이벤트가 발생하면 새 클라이언트를 등록하고,
 * OP_READ 이벤트가 발생하면 해당 채널의 ChannelSession에 데이터를 전달한다.
 * 스레드를 블로킹하지 않아 메모리 효율이 높지만,
 * Selector 스레드에서 블로킹 작업을 수행하면 전체 채널 처리가 지연된다.
 */
public class NonBlockingServer implements ChatServer {

    private static final Logger logger = LoggerFactory.getLogger(NonBlockingServer.class);
    private static final int PORT = 8080;

    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ChatUseCase chatUseCase;

    private Selector selector;
    private ServerSocketChannel serverChannel;
    private ExecutorService selectorExecutor;
    private volatile boolean running = false;

    public NonBlockingServer(LoginUseCase loginUseCase, LogoutUseCase logoutUseCase, ChatUseCase chatUseCase) {
        this.loginUseCase = loginUseCase;
        this.logoutUseCase = logoutUseCase;
        this.chatUseCase = chatUseCase;
    }

    @Override
    public void start() throws IOException {
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(PORT));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        running = true;
        selectorExecutor = Executors.newSingleThreadExecutor();
        selectorExecutor.submit(this::selectorLoop);
        logger.info("서버 시작 (NIO): 포트 {}", PORT);
    }

    @Override
    public void stop() {
        running = false;
        logoutUseCase.shutdownAll();
        selector.wakeup();
        try {
            if (serverChannel != null) serverChannel.close();
            if (selector != null) selector.close();
        } catch (IOException e) {
            logger.error("서버 종료 중 오류", e);
        }
        if (selectorExecutor != null) selectorExecutor.shutdown();
        logger.info("서버 종료 (NIO)");
    }

    private void selectorLoop() {
        while (running) {
            try {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (!key.isValid()) continue;
                    if (key.isAcceptable()) handleAccept();
                    else if (key.isReadable()) handleRead(key);
                }
            } catch (IOException e) {
                if (running) logger.error("셀렉터 오류", e);
            }
        }
    }

    private void handleAccept() throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        if (clientChannel == null) return;
        clientChannel.configureBlocking(false);
        NioSession nioSession = new NioSession(clientChannel);
        ChannelSession session = new ChannelSession(nioSession, loginUseCase, logoutUseCase, chatUseCase);
        clientChannel.register(selector, SelectionKey.OP_READ, new Attachment(session, ByteBuffer.allocate(1024)));
        logger.info("연결: {}", clientChannel.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) {
        Attachment att = (Attachment) key.attachment();
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            int bytesRead = channel.read(att.buffer());
            if (bytesRead == -1) { att.session().close(); key.cancel(); return; }
            att.buffer().flip();
            att.session().processBuffer(att.buffer());
            att.buffer().clear();
        } catch (IOException e) {
            logger.error("읽기 오류", e);
            att.session().close();
            key.cancel();
        }
    }

    private record Attachment(ChannelSession session, ByteBuffer buffer) {}
}
