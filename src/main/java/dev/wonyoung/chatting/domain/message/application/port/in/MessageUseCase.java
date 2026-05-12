package dev.wonyoung.chatting.domain.message.application.port.in;

import dev.wonyoung.chatting.domain.message.application.port.out.MessageSenderPort;

public interface MessageUseCase {
    void register(MessageSenderPort sender);
    void unregister(MessageSenderPort sender);
    void broadcast(String message, MessageSenderPort exclude);
}
