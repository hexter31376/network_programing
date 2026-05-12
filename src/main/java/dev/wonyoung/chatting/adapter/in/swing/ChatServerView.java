package dev.wonyoung.chatting.adapter.in.swing;

import javax.swing.*;
import java.awt.*;

public class ChatServerView extends JFrame {

    private final JTextArea chatArea;

    public ChatServerView() {
        super("Chat Server");

        chatArea = new JTextArea();
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 350);
        setVisible(true);
    }

    public void appendChat(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
}
