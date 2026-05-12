package dev.wonyoung.chatting.adapter.in.swing;

import dev.wonyoung.chatting.application.service.ChatServerService;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

@Component
public class ChatServerController {

    private final ChatServerView view;
    private final ChatServerService service;

    @Inject
    public ChatServerController(ChatServerView view, ChatServerService service) {
        this.view = view;
        this.service = service;
    }

    public void start() {
        service.setOnLog(view::appendChat);
        service.start();
    }
}
