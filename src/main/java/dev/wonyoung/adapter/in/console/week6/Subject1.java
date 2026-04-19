package dev.wonyoung.adapter.in.console.week6;

import dev.wonyoung.adapter.in.swing.week6.UdpChatPresenter;
import dev.wonyoung.adapter.in.swing.week6.UdpChatView;
import dev.wonyoung.adapter.out.udp.UdpSocketAdapter;
import dev.wonyoung.application.UdpChatService;
import dev.wonyoung.domain.port.in.ChatUseCase;
import dev.wonyoung.domain.port.out.UdpSocketPort;
import dev.wonyoung.infrastructure.container.di.Component;

import javax.swing.*;
import java.net.InetAddress;

@Component
public class Subject1 {

    public void start() {
        try {
            InetAddress localhost = InetAddress.getLocalHost();

            UdpSocketPort serverSocket = new UdpSocketAdapter(5000);
            ChatUseCase serverService = new UdpChatService(serverSocket);

            UdpSocketPort clientSocket = new UdpSocketAdapter(4000, localhost, 5000);
            ChatUseCase clientService = new UdpChatService(clientSocket);

            SwingUtilities.invokeLater(() -> {
                UdpChatView serverView = new UdpChatView("UDP server (port 5000)");
                new UdpChatPresenter(serverService, serverView).bind();

                UdpChatView clientView = new UdpChatView("UDP client (port 4000 -> 5000)");
                new UdpChatPresenter(clientService, clientView).bind();
            });
        } catch (Exception e) {
            throw new RuntimeException("Subject1 시작 실패: " + e.getMessage(), e);
        }
    }
}
