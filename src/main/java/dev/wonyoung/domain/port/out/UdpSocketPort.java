package dev.wonyoung.domain.port.out;

import dev.wonyoung.domain.model.ChatMessage;

import java.io.IOException;

public interface UdpSocketPort {
    void send(String message) throws IOException;
    ChatMessage receive() throws IOException;
    boolean isClosed();
    void close();
}
