package dev.wonyoung.chatting.adapter.in.swing;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ChatServerView extends JFrame {

    private final JTextArea chatTextArea;
    private final JTextField chatTextField;

    public ChatServerView() {
        super("Chat Server");

        chatTextArea = new JTextArea();
        chatTextArea.setLineWrap(true);
        chatTextArea.setWrapStyleWord(true);
        chatTextArea.setEditable(false);
        add(new JScrollPane(chatTextArea), BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("  text : "), BorderLayout.WEST);
        chatTextField = new JTextField(30);
        panel.add(chatTextField, BorderLayout.CENTER);
        add(panel, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 300);
        setVisible(true);
    }

    public void setOnSubmit(Consumer<String> handler) {
        chatTextField.addActionListener(e -> {
            handler.accept(chatTextField.getText());
            chatTextField.setText("");
        });
    }

    public void appendChat(String message) {
        chatTextArea.append(message + "\n");
        chatTextArea.setCaretPosition(chatTextArea.getDocument().getLength());
    }
}
