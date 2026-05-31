package dev.wonyoung.server.adapter.in.socket.async;

import dev.wonyoung.server.application.port.out.ClientSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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
