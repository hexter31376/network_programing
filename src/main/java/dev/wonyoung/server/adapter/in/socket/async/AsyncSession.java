package dev.wonyoung.server.adapter.in.socket.async;

import dev.wonyoung.server.application.port.out.ClientSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * NIO2 AsynchronousSocketChannel을 ClientSession 인터페이스로 감싼 세션 구현체다.
 *
 * channel.write가 Future를 반환하므로 get으로 완료를 기다려 모든 바이트를 전송한다.
 * 동시에 여러 스레드가 send를 호출할 수 있으므로 synchronized로 보호한다.
 */
public class AsyncSession implements ClientSession {

    private final String id;
    private final AsynchronousSocketChannel channel;

    public AsyncSession(AsynchronousSocketChannel channel) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.channel = channel;
    }

    @Override
    public String getId() { return id; }

    @Override
    public void close() {
        try { channel.close(); } catch (IOException ignored) {}
    }

    @Override
    public synchronized void send(String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap((message + "\n").getBytes(StandardCharsets.UTF_8));
        try {
            while (buffer.hasRemaining()) channel.write(buffer).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new IOException(e.getCause());
        }
    }
}
