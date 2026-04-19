package dev.wonyoung.adapter.in.swing.week6;

import javax.swing.*;
import java.awt.*;

public class VocabularyClientView extends JFrame {

    private final JTextField wordField = new JTextField(10);
    private final JTextField meaningField = new JTextField(20);
    private final JButton addButton = new JButton("ADD");
    private final JButton getButton = new JButton("GET");
    private final JButton deleteButton = new JButton("DELETE");
    private final JButton listButton = new JButton("LIST");
    private final JTextArea resultArea = new JTextArea();

    public VocabularyClientView() {
        super("UDP 단어장 클라이언트 (port 6001 → 6000)");

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("단어:"));
        inputPanel.add(wordField);
        inputPanel.add(new JLabel("뜻:"));
        inputPanel.add(meaningField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addButton);
        buttonPanel.add(getButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(listButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationByPlatform(true);
        setVisible(true);
    }

    public String getWord() { return wordField.getText().trim(); }
    public String getMeaning() { return meaningField.getText().trim(); }

    public void onAdd(Runnable handler) { addButton.addActionListener(e -> handler.run()); }
    public void onGet(Runnable handler) { getButton.addActionListener(e -> handler.run()); }
    public void onDelete(Runnable handler) { deleteButton.addActionListener(e -> handler.run()); }
    public void onList(Runnable handler) { listButton.addActionListener(e -> handler.run()); }

    public void appendResult(String text) {
        SwingUtilities.invokeLater(() -> {
            resultArea.append(text);
            resultArea.setCaretPosition(resultArea.getDocument().getLength());
        });
    }
}
