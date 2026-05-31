package dev.wonyoung.server.adapter.in.swing;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

/**
 * 서버 상태를 표시하고 시작 및 중지 버튼을 제공하는 Swing 뷰다.
 *
 * 시작 버튼과 중지 버튼, 상태 레이블, 로그 출력 영역으로 구성된다.
 * 로그 영역은 TextAreaAppender를 통해 Logback 로그를 실시간으로 표시한다.
 * 상태 레이블 업데이트는 EDT 스레드에서 안전하게 수행하도록 invokeLater를 사용한다.
 */
public class ChatServerView extends JFrame {

    private final JButton startButton;
    private final JButton stopButton;
    private final JLabel statusLabel;
    private final JTextArea logArea;

    public ChatServerView() {
        super("Chat Server");

        startButton = new JButton("시작");
        stopButton = new JButton("중지");
        stopButton.setEnabled(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        add(buttonPanel, BorderLayout.NORTH);

        statusLabel = new JLabel("대기 중");
        add(statusLabel, BorderLayout.CENTER);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(
                logArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        scrollPane.setPreferredSize(new Dimension(500, 250));
        add(scrollPane, BorderLayout.SOUTH);

        setSize(520, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public JButton getStartButton() { return startButton; }
    public JButton getStopButton()  { return stopButton; }
    public JTextArea getLogArea()   { return logArea; }

    public void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
    }
}
