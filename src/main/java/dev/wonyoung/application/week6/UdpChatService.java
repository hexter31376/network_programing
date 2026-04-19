package dev.wonyoung.application.week6;

import dev.wonyoung.domain.model.ChatMessage;
import dev.wonyoung.domain.port.in.ChatUseCase;
import dev.wonyoung.domain.port.out.UdpSocketPort;

import java.util.function.Consumer;

public class UdpChatService implements ChatUseCase {

    private final UdpSocketPort socketPort;

    public UdpChatService(UdpSocketPort socketPort) {
        this.socketPort = socketPort;
    }

    @Override
    public void sendMessage(String message) {
        try {
            socketPort.send(message);
        } catch (Exception e) {
            throw new RuntimeException("message send failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void startReceiving(Consumer<ChatMessage> onReceive) {
        Thread receiver = new Thread(() -> {
            while (!socketPort.isClosed()) {
                try {
                    ChatMessage message = socketPort.receive();
                    onReceive.accept(message);
                } catch (Exception e) {
                    if (!socketPort.isClosed()) {
                        throw new RuntimeException("packet send failed: " + e.getMessage(), e);
                    }
                }
            }
        }, "UDP-Receiver");
        receiver.setDaemon(true);
        receiver.start();
    }

    @Override
    public void stop() {
        socketPort.close();
    }
}
