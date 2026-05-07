package dev.wonyoung.domain.port.in;

import dev.wonyoung.domain.model.ChatMessage;

import java.util.function.Consumer;

public interface ChatUseCase {
    void sendMessage(String message);
    void startReceiving(Consumer<ChatMessage> onReceive);
    void stop();
}
