package dev.wonyoung.chatting.adapter.in.swing;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ChatClientView extends JFrame {

    private final JTextArea chatTextArea;
    private final JTextField chatTextField;
    private final JButton reconnectButton;

    public ChatClientView() {
        super("Chat Client");

        chatTextArea = new JTextArea();
        chatTextArea.setLineWrap(true);
        chatTextArea.setWrapStyleWord(true);
        chatTextArea.setEditable(false);
        add(new JScrollPane(chatTextArea), BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("  text : "), BorderLayout.WEST);
        chatTextField = new JTextField(20);
        panel.add(chatTextField, BorderLayout.CENTER);
        reconnectButton = new JButton("Reconnect");
        panel.add(reconnectButton, BorderLayout.EAST);
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

    public void setOnReconnect(Runnable handler) {
        reconnectButton.addActionListener(e -> handler.run());
    }

    public void appendChat(String message) {
        chatTextArea.append(message + "\n");
        chatTextArea.setCaretPosition(chatTextArea.getDocument().getLength());
    }
}
