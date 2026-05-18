package dev.wonyoung.chatting.server;

import dev.wonyoung.chatting.server.adapter.in.ServerSwingView;
import dev.wonyoung.chatting.server.adapter.in.TcpLoginServer;
import dev.wonyoung.chatting.server.application.port.out.ServerEventPort;
import dev.wonyoung.common.container.Container;
import dev.wonyoung.common.container.aop.handler.ExceptionHandlingInterceptor;

import javax.swing.*;

public class ServerMain {

    public static void main(String[] args) throws Exception {
        // 1. GUI 먼저 생성 (EDT에서)
        ServerSwingView view = new ServerSwingView();

        // 2. 컨테이너 구성 — view를 ServerEventPort 구현체로 등록
        Container container = new Container("dev.wonyoung.chatting.server");
        container.addInterceptor(new ExceptionHandlingInterceptor());
        container.register(ServerEventPort.class, view);

        // 3. TCP 서버는 블로킹 루프이므로 별도 스레드에서 실행
        TcpLoginServer server = container.get(TcpLoginServer.class);
        Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(view, "서버 오류: " + e.getMessage(),
                        "오류", JOptionPane.ERROR_MESSAGE));
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }
}

