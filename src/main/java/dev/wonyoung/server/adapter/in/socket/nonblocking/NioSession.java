package dev.wonyoung.server.adapter.in.socket.nonblocking;

import dev.wonyoung.server.application.port.out.ClientSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * NIO SocketChannel을 ClientSession 인터페이스로 감싼 세션 구현체다.
 *
 * SocketChannel.write는 논블로킹이므로 모든 바이트가 전송될 때까지
 * hasRemaining으로 반복 전송을 보장한다.
 * 동시에 여러 스레드가 send를 호출할 수 있으므로 synchronized로 보호한다.
 */
public class NioSession implements ClientSession {

    private final String id;
    private final SocketChannel channel;

    public NioSession(SocketChannel channel) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.channel = channel;
    }

    @Override
    public String getId() { return id; }

    @Override
    public synchronized void send(String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap((message + "\n").getBytes(StandardCharsets.UTF_8));
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    @Override
    public void close() {
        try { channel.close(); } catch (IOException ignored) {}
    }
}
