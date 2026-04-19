package dev.wonyoung.adapter.in.console.week6;

import dev.wonyoung.adapter.in.swing.week6.AccountClientPresenter;
import dev.wonyoung.adapter.in.swing.week6.AccountClientView;
import dev.wonyoung.adapter.in.swing.week6.AccountServerView;
import dev.wonyoung.adapter.out.repository.CsvAccountRepository;
import dev.wonyoung.adapter.out.udp.AccountServerAdapter;
import dev.wonyoung.adapter.out.udp.UdpSocketAdapter;
import dev.wonyoung.application.AccountService;
import dev.wonyoung.application.UdpChatService;
import dev.wonyoung.domain.port.in.AccountUseCase;
import dev.wonyoung.domain.port.out.UdpSocketPort;
import dev.wonyoung.infrastructure.container.di.Component;

import javax.swing.*;
import java.net.InetAddress;

@Component
public class Subject3 {

    public void start() {
        try {
            InetAddress localhost = InetAddress.getLocalHost();

            // 서버: port 7000, 계좌 데이터 관리
            UdpSocketPort serverSocket = new UdpSocketAdapter(7000);
            AccountUseCase accountService = new AccountService(new CsvAccountRepository("accounts.csv"));
            AccountServerAdapter serverAdapter = new AccountServerAdapter(serverSocket, accountService);

            // 클라이언트: port 7001 → 7000
            UdpSocketPort clientSocket = new UdpSocketAdapter(7001, localhost, 7000);
            UdpChatService clientService = new UdpChatService(clientSocket);

            SwingUtilities.invokeLater(() -> {
                AccountServerView serverView = new AccountServerView();
                serverAdapter.setLogger(serverView::appendLog);
                serverAdapter.setAccountsRefresher(serverView::updateAccounts);
                serverAdapter.start();

                AccountClientView clientView = new AccountClientView();
                new AccountClientPresenter(clientService, clientView).bind();
            });
        } catch (Exception e) {
            throw new RuntimeException("Subject3 시작 실패: " + e.getMessage(), e);
        }
    }
}
