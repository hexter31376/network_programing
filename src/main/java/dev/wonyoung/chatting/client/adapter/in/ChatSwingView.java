package dev.wonyoung.chatting.client.adapter.in;

import dev.wonyoung.chatting.client.application.service.ChatService;
import dev.wonyoung.chatting.share.domain.Protocol;
import dev.wonyoung.chatting.share.dto.ProtocolMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ChatSwingView extends JFrame {

    private final ChatService chatService;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);

    private final JTextField idField = new JTextField(15);
    private final JLabel loginErrorLabel = new JLabel(" ");

    private final JTextArea chatArea = new JTextArea();
    private final JTextField messageField = new JTextField();

    public ChatSwingView(ChatService chatService) {
        this.chatService = chatService;
        chatService.startReceiving(this::onMessageReceived);
        initUI();
    }

    private void initUI() {
        setTitle("멀티캐스트 채팅");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(520, 620);
        setLocationRelativeTo(null);

        mainPanel.add(buildLoginPanel(), "login");
        mainPanel.add(buildChatPanel(), "chat");
        add(mainPanel);
        cardLayout.show(mainPanel, "login");

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (chatService.isLoggedIn()) {
                    chatService.logout();
                }
                dispose();
                System.exit(0);
            }
        });

        setVisible(true);
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(new JLabel("아이디를 입력하세요"), gbc);

        gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(idField, gbc);

        JButton loginButton = new JButton("로그인");
        gbc.gridx = 1;
        panel.add(loginButton, gbc);

        loginErrorLabel.setForeground(Color.RED);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(loginErrorLabel, gbc);

        loginButton.addActionListener(e -> doLogin());
        idField.addActionListener(e -> doLogin());

        return panel;
    }

    private JPanel buildChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        panel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutButton = new JButton("로그아웃");
        topBar.add(logoutButton);
        panel.add(topBar, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new BorderLayout(4, 0));
        JButton sendButton = new JButton("전송");
        bottom.add(messageField, BorderLayout.CENTER);
        bottom.add(sendButton, BorderLayout.EAST);
        panel.add(bottom, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> doSend());
        messageField.addActionListener(e -> doSend());
        logoutButton.addActionListener(e -> doLogout());

        return panel;
    }

    private void doLogin() {
        String userId = idField.getText().trim();
        if (userId.isEmpty()) {
            loginErrorLabel.setText("아이디를 입력하세요.");
            return;
        }
        loginErrorLabel.setText("연결 중...");
        // TCP 연결·응답 대기가 EDT를 블로킹하지 않도록 백그라운드 스레드에서 실행
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return chatService.login(userId);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        loginErrorLabel.setText(" ");
                        idField.setText("");
                        setTitle("멀티캐스트 채팅 - " + userId);
                        cardLayout.show(mainPanel, "chat");
                    } else {
                        loginErrorLabel.setText("중복된 아이디입니다.");
                    }
                } catch (Exception e) {
                    loginErrorLabel.setText("서버 연결 실패.");
                }
            }
        }.execute();
    }

    private void doSend() {
        String text = messageField.getText().trim();
        if (text.isEmpty()) return;
        chatService.send(text);
        messageField.setText("");
    }

    private void doLogout() {
        chatService.logout();
        setTitle("멀티캐스트 채팅");
        chatArea.setText("");
        cardLayout.show(mainPanel, "login");
    }

    private void onMessageReceived(ProtocolMessage message) {
        SwingUtilities.invokeLater(() -> {
            String line = switch (message.type) {
                case JOIN -> "*** " + message.sender + " 님이 입장하였습니다. ***";
                case LEAVE -> "*** " + message.sender + " 님이 퇴장하였습니다. ***";
                case CHAT -> "[" + message.sender + "]: " + message.message;
                default -> null;
            };
            if (line != null) {
                chatArea.append(line + "\n");
            }
        });
    }
}
