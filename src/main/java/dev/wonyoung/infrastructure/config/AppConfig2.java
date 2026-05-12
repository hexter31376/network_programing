package dev.wonyoung.infrastructure.config;

import dev.wonyoung.chatting2.adapter.in.swing.ChatServer2Controller;
import dev.wonyoung.chatting2.adapter.in.swing.ChatServer2View;
import dev.wonyoung.infrastructure.container.Container;
import dev.wonyoung.infrastructure.container.aop.handler.ExceptionHandlingInterceptor;

public class AppConfig2 {

    public void startApp() {
        try {
            init();
        } catch (Exception e) {
            System.err.println("[FATAL] 애플리케이션 시작 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void init() throws Exception {
        Container container = new Container("dev.wonyoung.chatting2");
        container.addInterceptor(new ExceptionHandlingInterceptor());

        ChatServer2View view = new ChatServer2View();
        container.register(ChatServer2View.class, view);

        ChatServer2Controller controller = container.get(ChatServer2Controller.class);
        controller.start();
    }
}
