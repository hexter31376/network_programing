package dev.wonyoung.chatting.server.adapter.in;

import dev.wonyoung.chatting.server.application.port.out.ServerEventPort;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ServerSwingView extends JFrame implements ServerEventPort {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final DefaultListModel<String> userListModel = new DefaultListModel<>();
    private final JTextArea logArea = new JTextArea();
    private final JLabel statusLabel = new JLabel("대기 중...");

    public ServerSwingView() {
        initUI();
    }

    private void initUI() {
        setTitle("멀티캐스트 채팅 서버");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            buildUserPanel(), buildLogPanel());
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.28);

        add(splitPane, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel buildUserPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBorder(BorderFactory.createTitledBorder("접속 사용자"));

        JList<String> userList = new JList<>(userListModel);
        userList.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        panel.add(new JScrollPane(userList), BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBorder(BorderFactory.createTitledBorder("서버 로그"));

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bar.setBorder(BorderFactory.createEtchedBorder());
        bar.add(new JLabel("상태:"));
        bar.add(statusLabel);
        return bar;
    }

    // ServerEventPort 구현

    @Override
    public void onServerStarted(int port) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setForeground(new Color(0, 140, 0));
            statusLabel.setText("실행 중  |  TCP :" + port);
            appendLog("서버 시작 — TCP 포트 " + port + " 에서 대기 중");
        });
    }

    @Override
    public void onUserLoggedIn(String userId) {
        SwingUtilities.invokeLater(() -> {
            userListModel.addElement(userId);
            updateTitle();
            appendLog(userId + " 로그인");
        });
    }

    @Override
    public void onUserLoggedOut(String userId) {
        SwingUtilities.invokeLater(() -> {
            userListModel.removeElement(userId);
            updateTitle();
            appendLog(userId + " 로그아웃");
        });
    }

    // 내부 헬퍼

    private void appendLog(String message) {
        String time = LocalTime.now().format(TIME_FMT);
        logArea.append("[" + time + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void updateTitle() {
        setTitle("멀티캐스트 채팅 서버  (" + userListModel.size() + "명 접속 중)");
    }
}
