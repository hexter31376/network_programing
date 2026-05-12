package dev.wonyoung.chatting.domain.message.application.service;

import dev.wonyoung.chatting.domain.message.application.port.in.MessageUseCase;
import dev.wonyoung.chatting.domain.message.application.port.out.MessageSenderPort;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class MessageService implements MessageUseCase {

    private final List<MessageSenderPort> senders = new CopyOnWriteArrayList<>();

    @Inject
    public MessageService() {}

    @Override
    public void register(MessageSenderPort sender) {
        senders.add(sender);
    }

    @Override
    public void unregister(MessageSenderPort sender) {
        senders.remove(sender);
    }

    @Override
    public void broadcast(String message, MessageSenderPort exclude) {
        senders.stream()
                .filter(s -> s != exclude)
                .forEach(s -> s.send(message));
    }
}
