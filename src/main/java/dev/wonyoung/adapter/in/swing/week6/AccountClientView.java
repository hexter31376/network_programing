package dev.wonyoung.adapter.in.swing.week6;

import javax.swing.*;
import java.awt.*;

public class AccountClientView extends JFrame {

    private final JTextField addAccountField = new JTextField(10);
    private final JTextField addNameField = new JTextField(8);
    private final JTextField addBalanceField = new JTextField(8);
    private final JButton addButton = new JButton("등록");

    private final JTextField getAccountField = new JTextField(10);
    private final JButton getButton = new JButton("조회");
    private final JButton listButton = new JButton("전체목록");

    private final JTextField updateAccountField = new JTextField(10);
    private final JTextField updateNameField = new JTextField(8);
    private final JTextField updateBalanceField = new JTextField(8);
    private final JButton updateButton = new JButton("수정");

    private final JTextField deleteAccountField = new JTextField(10);
    private final JButton deleteButton = new JButton("삭제");

    private final JTextArea resultArea = new JTextArea();

    public AccountClientView() {
        super("계좌 관리 클라이언트 (port 7001 -> 7000)");

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addPanel.setBorder(BorderFactory.createTitledBorder("고객 등록"));
        addPanel.add(new JLabel("계좌번호:")); addPanel.add(addAccountField);
        addPanel.add(new JLabel("이름:"));    addPanel.add(addNameField);
        addPanel.add(new JLabel("잔고:"));    addPanel.add(addBalanceField);
        addPanel.add(addButton);

        JPanel getPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        getPanel.setBorder(BorderFactory.createTitledBorder("계좌 조회"));
        getPanel.add(new JLabel("계좌번호:")); getPanel.add(getAccountField);
        getPanel.add(getButton);
        getPanel.add(listButton);
        getAccountField.addActionListener(e -> getButton.doClick());

        JPanel updatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        updatePanel.setBorder(BorderFactory.createTitledBorder("계좌 수정"));
        updatePanel.add(new JLabel("계좌번호:")); updatePanel.add(updateAccountField);
        updatePanel.add(new JLabel("새 이름:")); updatePanel.add(updateNameField);
        updatePanel.add(new JLabel("새 잔고:")); updatePanel.add(updateBalanceField);
        updatePanel.add(updateButton);

        JPanel deletePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        deletePanel.setBorder(BorderFactory.createTitledBorder("계좌 삭제"));
        deletePanel.add(new JLabel("계좌번호:")); deletePanel.add(deleteAccountField);
        deletePanel.add(deleteButton);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(addPanel);
        topPanel.add(getPanel);
        topPanel.add(updatePanel);
        topPanel.add(deletePanel);

        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 600);
        setLocationByPlatform(true);
        setVisible(true);
    }

    public String getAddAccount() {
        return addAccountField.getText().trim();
    }
    public String getAddName() {
        return addNameField.getText().trim();
    }
    public String getAddBalance() {
        return addBalanceField.getText().trim();
    }
    public String getGetAccount() {
        return getAccountField.getText().trim();
    }
    public String getUpdateAccount() {
        return updateAccountField.getText().trim();
    }
    public String getUpdateName() {
        return updateNameField.getText().trim();
    }
    public String getUpdateBalance() {
        return updateBalanceField.getText().trim();
    }
    public String getDeleteAccount() {
        return deleteAccountField.getText().trim();
    }

    public void onAdd(Runnable handler)    { addButton.addActionListener(e -> handler.run()); }
    public void onGet(Runnable handler)    { getButton.addActionListener(e -> handler.run()); }
    public void onList(Runnable handler)   { listButton.addActionListener(e -> handler.run()); }
    public void onUpdate(Runnable handler) { updateButton.addActionListener(e -> handler.run()); }
    public void onDelete(Runnable handler) { deleteButton.addActionListener(e -> handler.run()); }

    public void appendResult(String text) {
        SwingUtilities.invokeLater(() -> {
            resultArea.append(text);
            resultArea.setCaretPosition(resultArea.getDocument().getLength());
        });
    }
}
