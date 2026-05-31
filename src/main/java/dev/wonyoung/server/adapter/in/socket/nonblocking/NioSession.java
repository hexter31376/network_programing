package dev.wonyoung.server.adapter.in.socket.nonblocking;

import dev.wonyoung.server.application.port.out.ClientSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

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
