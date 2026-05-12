package dev.wonyoung.chatting.adapter.in.swing;

import dev.wonyoung.chatting.application.service.ChatClientService;

public class ChatClientController {

    private final ChatClientView view;
    private final ChatClientService service;

    public ChatClientController(ChatClientView view, ChatClientService service) {
        this.view = view;
        this.service = service;
    }

    public void init() {
        service.setOnMessage(view::appendChat);
        service.setOnAccepted(() -> {
            view.appendChat("연결 성공!");
            view.switchToChatMode();
        });
        service.setOnRejected(reason -> {
            view.showError(reason);
            view.switchToLoginMode();
        });
        service.setOnDisconnected(() -> {
            view.appendChat("서버와 연결이 끊어졌습니다.");
            view.switchToLoginMode();
        });

        view.setOnConnect(username -> {
            view.appendChat("연결 시도 중...");
            service.connect(username);
        });
        view.setOnSubmit(text -> {
            service.send(text);
            view.appendChat("[나] " + text);
        });
    }
}
