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
