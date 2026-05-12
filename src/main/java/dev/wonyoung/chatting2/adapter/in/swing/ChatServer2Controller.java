package dev.wonyoung.chatting2.adapter.in.swing;

import dev.wonyoung.chatting2.application.service.ChatServer2Service;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

@Component
public class ChatServer2Controller {

    private final ChatServer2View view;
    private final ChatServer2Service service;

    @Inject
    public ChatServer2Controller(ChatServer2View view, ChatServer2Service service) {
        this.view = view;
        this.service = service;
    }

    public void start() {
        service.setOnLog(view::appendLog);
        service.start();
    }
}
