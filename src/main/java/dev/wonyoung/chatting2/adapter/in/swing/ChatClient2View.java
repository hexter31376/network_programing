package dev.wonyoung.chatting2.adapter.in.swing;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ChatClient2View extends JFrame {

    private final JTextArea chatArea;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel bottomPanel = new JPanel(cardLayout);

    private final JTextField usernameField = new JTextField();
    private final JButton loginButton = new JButton("로그인");

    private final JButton logoutButton = new JButton("로그아웃");
    private final JTextField messageField = new JTextField();
    private final JButton sendButton = new JButton("전송");

    public ChatClient2View() {
        super("Chat Client 2");

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // 로그인 카드: [ 이름: ][ 입력창 ][ 로그인 ]
        JPanel loginCard = new JPanel(new BorderLayout());
        loginCard.add(new JLabel("  이름: "), BorderLayout.WEST);
        loginCard.add(usernameField, BorderLayout.CENTER);
        loginCard.add(loginButton, BorderLayout.EAST);

        // 채팅 카드: [ 로그아웃 ][ 메시지 입력창 ][ 전송 ]
        JPanel chatCard = new JPanel(new BorderLayout());
        chatCard.add(logoutButton, BorderLayout.WEST);
        chatCard.add(messageField, BorderLayout.CENTER);
        chatCard.add(sendButton, BorderLayout.EAST);

        bottomPanel.add(loginCard, "LOGIN");
        bottomPanel.add(chatCard, "CHAT");
        add(bottomPanel, BorderLayout.SOUTH);

        usernameField.addActionListener(e -> loginButton.doClick());
        messageField.addActionListener(e -> sendButton.doClick());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(460, 400);
        setVisible(true);
    }

    public void setOnLogin(Consumer<String> handler) {
        loginButton.addActionListener(e -> {
            String name = usernameField.getText().trim();
            if (!name.isBlank()) handler.accept(name);
        });
    }

    public void setOnSend(Consumer<String> handler) {
        sendButton.addActionListener(e -> {
            String text = messageField.getText().trim();
            if (!text.isBlank()) {
                handler.accept(text);
                messageField.setText("");
            }
        });
    }

    public void setOnLogout(Runnable handler) {
        logoutButton.addActionListener(e -> handler.run());
    }

    public void switchToChat() {
        cardLayout.show(bottomPanel, "CHAT");
        usernameField.setText("");
        messageField.requestFocus();
    }

    public void switchToLogin() {
        cardLayout.show(bottomPanel, "LOGIN");
        usernameField.requestFocus();
    }

    public void appendChat(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    public void showError(String message) {
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(this, message, "오류", JOptionPane.ERROR_MESSAGE));
    }
}
