package dev.wonyoung.client.adapter.in.swing;

import dev.wonyoung.client.adapter.out.socket.SocketChatClient;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class ChatClientController {

    private final ChatClientView view;
    private final SocketChatClient client;

    public ChatClientController(ChatClientView view, SocketChatClient client) {
        this.view = view;
        this.client = client;
        bindEvents();
    }

    private void bindEvents() {
        view.getConnectButton().addActionListener(this::onConnect);
        view.getDisconnectButton().addActionListener(e -> onDisconnect());
        view.getSendButton().addActionListener(e -> onSend());
        view.getMessageField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) onSend();
            }
        });
    }

    private void onConnect(ActionEvent e) {
        String host = view.getHostField().getText().trim();
        String portText = view.getPortField().getText().trim();
        String nickname = view.getNicknameField().getText().trim();

        if (nickname.isEmpty()) {
            JOptionPane.showMessageDialog(view, "닉네임을 입력하세요.");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "포트 번호가 올바르지 않습니다.");
            return;
        }

        // new Socket()은 블로킹 IO → EDT에서 직접 호출하면 UI가 멈춤
        view.getConnectButton().setEnabled(false);
        view.updateStatus("연결 중...");

        Thread connectThread = new Thread(() -> {
            try {
                client.connect(host, port, nickname, this::onMessageReceived, this::onServerDisconnect);
                SwingUtilities.invokeLater(() -> {
                    view.setConnected(true);
                    view.updateStatus(host + ":" + port + " 연결됨 (" + nickname + ")");
                    view.appendChat(">> " + host + ":" + port + " 에 연결되었습니다.");
                });
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    view.getConnectButton().setEnabled(true);
                    view.updateStatus("연결 안됨");
                    JOptionPane.showMessageDialog(view, "연결 실패: " + ex.getMessage());
                });
            }
        });
        connectThread.setDaemon(true);
        connectThread.start();
    }

    private void onDisconnect() {
        client.disconnect();
        view.setConnected(false);
        view.updateStatus("연결 안됨");
        view.appendChat(">> 연결을 종료했습니다.");
    }

    private void onSend() {
        String message = view.getMessageField().getText();
        if (message.isBlank() || !client.isConnected()) return;

        try {
            client.send(message);
            view.getMessageField().setText("");
        } catch (IOException e) {
            view.appendChat(">> 전송 실패: " + e.getMessage());
        }
    }

    private void onMessageReceived(String message) {
        view.appendChat(message);
    }

    private void onServerDisconnect() {
        if (client.isConnected()) return;
        view.setConnected(false);
        view.updateStatus("연결 안됨");
        view.appendChat(">> 서버와의 연결이 끊어졌습니다.");
    }
}
