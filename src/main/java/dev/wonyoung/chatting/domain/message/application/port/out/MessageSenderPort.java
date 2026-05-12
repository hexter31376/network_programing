package dev.wonyoung.chatting.domain.message.application.port.out;

public interface MessageSenderPort {
    void send(String message);
}
