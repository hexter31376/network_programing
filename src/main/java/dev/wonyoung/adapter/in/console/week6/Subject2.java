package dev.wonyoung.adapter.in.console.week6;

import dev.wonyoung.adapter.in.swing.week6.UdpChatView;
import dev.wonyoung.adapter.in.swing.week6.VocabularyClientPresenter;
import dev.wonyoung.adapter.in.swing.week6.VocabularyClientView;
import dev.wonyoung.adapter.out.file.FileVocabularyRepository;
import dev.wonyoung.adapter.out.udp.UdpSocketAdapter;
import dev.wonyoung.adapter.out.udp.VocabularyServerAdapter;
import dev.wonyoung.application.UdpChatService;
import dev.wonyoung.application.VocabularyService;
import dev.wonyoung.domain.port.in.ChatUseCase;
import dev.wonyoung.domain.port.out.UdpSocketPort;
import dev.wonyoung.infrastructure.container.di.Component;

import javax.swing.*;
import java.net.InetAddress;

@Component
public class Subject2 {

    public void start() {
        try {
            InetAddress localhost = InetAddress.getLocalHost();

            // 서버
            UdpSocketPort serverSocket = new UdpSocketAdapter(6000);
            VocabularyService vocabularyService = new VocabularyService(new FileVocabularyRepository("vocabulary.csv"));
            VocabularyServerAdapter serverAdapter = new VocabularyServerAdapter(serverSocket, vocabularyService);

            // 클라이언트
            UdpSocketPort clientSocket = new UdpSocketAdapter(6001, localhost, 6000);
            ChatUseCase clientService = new UdpChatService(clientSocket);

            SwingUtilities.invokeLater(() -> {
                UdpChatView serverView = new UdpChatView("UDP 단어장 서버 (port 6000)");
                serverAdapter.setLogger(serverView::appendMessage);
                serverAdapter.start();

                VocabularyClientView clientView = new VocabularyClientView();
                new VocabularyClientPresenter(clientService, clientView).bind();
            });
        } catch (Exception e) {
            throw new RuntimeException("Subject2 시작 실패: " + e.getMessage(), e);
        }
    }
}
