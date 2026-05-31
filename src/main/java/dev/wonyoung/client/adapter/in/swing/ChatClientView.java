package dev.wonyoung.client.adapter.in.swing;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class ChatClientView extends JFrame {

    private final JTextField hostField;
    private final JTextField portField;
    private final JTextField nicknameField;
    private final JButton connectButton;
    private final JButton disconnectButton;
    private final JTextArea chatArea;
    private final JTextField messageField;
    private final JButton sendButton;
    private final JLabel statusLabel;

    public ChatClientView() {
        super("Chat Client");

        // 상단 연결 패널
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);

        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("호스트:"), gbc);
        gbc.gridx = 1;
        hostField = new JTextField("localhost", 10);
        topPanel.add(hostField, gbc);

        gbc.gridx = 2;
        topPanel.add(new JLabel("포트:"), gbc);
        gbc.gridx = 3;
        portField = new JTextField("8080", 5);
        topPanel.add(portField, gbc);

        gbc.gridx = 4;
        topPanel.add(new JLabel("닉네임:"), gbc);
        gbc.gridx = 5;
        nicknameField = new JTextField(8);
        topPanel.add(nicknameField, gbc);

        gbc.gridx = 6;
        connectButton = new JButton("연결");
        topPanel.add(connectButton, gbc);

        gbc.gridx = 7;
        disconnectButton = new JButton("연결끊기");
        disconnectButton.setEnabled(false);
        topPanel.add(disconnectButton, gbc);

        add(topPanel, BorderLayout.NORTH);

        // 중앙 채팅 로그
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setPreferredSize(new Dimension(580, 320));
        add(scrollPane, BorderLayout.CENTER);

        // 하단 메시지 입력 패널
        JPanel bottomPanel = new JPanel(new BorderLayout(4, 0));
        statusLabel = new JLabel("연결 안됨");
        messageField = new JTextField();
        messageField.setEnabled(false);
        sendButton = new JButton("전송");
        sendButton.setEnabled(false);

        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        setSize(650, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void appendChat(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    public void setConnected(boolean connected) {
        SwingUtilities.invokeLater(() -> {
            connectButton.setEnabled(!connected);
            disconnectButton.setEnabled(connected);
            hostField.setEnabled(!connected);
            portField.setEnabled(!connected);
            nicknameField.setEnabled(!connected);
            messageField.setEnabled(connected);
            sendButton.setEnabled(connected);
        });
    }

    public void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
    }

    public JTextField getHostField()      { return hostField; }
    public JTextField getPortField()      { return portField; }
    public JTextField getNicknameField()  { return nicknameField; }
    public JButton getConnectButton()     { return connectButton; }
    public JButton getDisconnectButton()  { return disconnectButton; }
    public JTextField getMessageField()   { return messageField; }
    public JButton getSendButton()        { return sendButton; }
}
