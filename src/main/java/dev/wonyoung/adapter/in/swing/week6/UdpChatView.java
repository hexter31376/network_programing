package dev.wonyoung.adapter.in.swing.week6;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class UdpChatView extends JFrame {

    private final JTextField messageInputField = new JTextField();
    private final JTextArea messageViewArea = new JTextArea();

    public UdpChatView(String title) {
        super(title);

        messageViewArea.setEditable(false);


        add(messageInputField, BorderLayout.NORTH);
        add(new JScrollPane(messageViewArea), BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 400);
        setLocationByPlatform(true);
        setVisible(true);
    }

    public void setOnSubmit(Consumer<String> handler) {
        messageInputField.addActionListener(e -> {
            String text = messageInputField.getText().trim();
            if (!text.isEmpty()) {
                handler.accept(text);
                messageInputField.setText("");
            }
        });
    }

    public void appendMessage(String text) {
        SwingUtilities.invokeLater(() -> {
            messageViewArea.append(text);
            messageViewArea.setCaretPosition(messageViewArea.getDocument().getLength());
        });
    }
}
