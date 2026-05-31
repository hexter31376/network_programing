package dev.wonyoung.server.adapter.in.swing;

import dev.wonyoung.server.adapter.in.socket.ChatServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ChatServerController {

    private static final Logger logger = LoggerFactory.getLogger(ChatServerController.class);

    private final ChatServerView view;
    private final ChatServer server;

    public ChatServerController(ChatServerView view, ChatServer server) {
        this.view = view;
        this.server = server;
        initView();
    }

    private void initView() {
        view.getStartButton().addActionListener(e -> startServer());
        view.getStopButton().addActionListener(e -> stopServer());
    }

    private void startServer() {
        try {
            server.start();
            view.getStartButton().setEnabled(false);
            view.getStopButton().setEnabled(true);
            view.updateStatus("실행 중 (포트 8080)");
        } catch (IOException e) {
            logger.error("서버 시작 실패: {}", e.getMessage());
            view.updateStatus("시작 실패: " + e.getMessage());
        }
    }

    private void stopServer() {
        server.stop();
        view.getStartButton().setEnabled(true);
        view.getStopButton().setEnabled(false);
        view.updateStatus("중지됨");
    }
}
