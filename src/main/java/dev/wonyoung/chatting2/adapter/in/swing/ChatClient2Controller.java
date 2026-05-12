package dev.wonyoung.chatting2.adapter.in.swing;

import dev.wonyoung.chatting2.application.service.ChatClient2Service;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ChatClient2Controller {

    private final ChatClient2View view;
    private final ChatClient2Service service;

    public ChatClient2Controller(ChatClient2View view, ChatClient2Service service) {
        this.view = view;
        this.service = service;
    }

    public void init() {
        service.setOnMessage(view::appendChat);
        service.setOnAccepted(() -> {
            view.appendChat("로그온 성공!");
            view.switchToChat();
        });
        service.setOnRejected(reason -> {
            view.showError(reason);
            view.switchToLogin();
        });
        service.setOnDisconnected(() -> {
            view.appendChat("서버와 연결이 끊어졌습니다.");
            view.switchToLogin();
        });

        view.setOnLogin(username -> {
            view.appendChat("연결 시도 중...");
            service.connect(username);
        });
        view.setOnSend(text -> {
            service.send(text);
            view.appendChat("[나] " + text);
        });
        view.setOnLogout(() -> {
            service.logout();
            view.appendChat("로그아웃했습니다.");
            view.switchToLogin();
        });

        view.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                service.disconnect();
                System.exit(0);
            }
        });
    }
}
