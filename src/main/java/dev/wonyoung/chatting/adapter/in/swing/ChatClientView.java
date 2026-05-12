package dev.wonyoung.chatting.adapter.in.swing;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ChatClientView extends JFrame {

    private final JTextArea chatArea;

    private final JPanel loginPanel;
    private final JTextField nameField;

    private final JPanel chatInputPanel;
    private final JTextField messageField;

    public ChatClientView() {
        super("Chat Client");

        chatArea = new JTextArea();
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        loginPanel = new JPanel(new BorderLayout());
        loginPanel.setBorder(BorderFactory.createTitledBorder("접속"));
        nameField = new JTextField();
        loginPanel.add(new JLabel("  이름: "), BorderLayout.WEST);
        loginPanel.add(nameField, BorderLayout.CENTER);
        add(loginPanel, BorderLayout.NORTH);

        chatInputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        chatInputPanel.add(new JLabel("  메시지: "), BorderLayout.WEST);
        chatInputPanel.add(messageField, BorderLayout.CENTER);
        chatInputPanel.setVisible(false);
        add(chatInputPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 350);
        setVisible(true);
    }

    public void setOnConnect(Consumer<String> handler) {
        nameField.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (!name.isBlank()) handler.accept(name);
        });
    }

    public void setOnSubmit(Consumer<String> handler) {
        messageField.addActionListener(e -> {
            String text = messageField.getText();
            if (!text.isBlank()) {
                handler.accept(text);
                messageField.setText("");
            }
        });
    }

    public void switchToChatMode() {
        SwingUtilities.invokeLater(() -> {
            loginPanel.setVisible(false);
            chatInputPanel.setVisible(true);
            messageField.requestFocusInWindow();
            revalidate();
            repaint();
        });
    }

    public void switchToLoginMode() {
        SwingUtilities.invokeLater(() -> {
            loginPanel.setVisible(true);
            chatInputPanel.setVisible(false);
            nameField.setText("");
            revalidate();
            repaint();
        });
    }

    public void showError(String message) {
        SwingUtilities.invokeLater(() -> appendChat("[오류] " + message));
    }

    public void appendChat(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
}
