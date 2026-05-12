package dev.wonyoung.chatting2.adapter.in.swing;

import javax.swing.*;
import java.awt.*;

public class ChatServer2View extends JFrame {

    private final JTextArea logArea;

    public ChatServer2View() {
        super("Chat Server 2");

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 300);
        setVisible(true);
    }

    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}
