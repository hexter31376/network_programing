package dev.wonyoung.adapter.in.swing.week2.day1.subject.view;

import dev.wonyoung.infrastructure.container.di.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

@Component
public class ViewImpl extends JFrame implements AccountView {

    // 등록 패널
    private JTextField regNameField;
    private JTextField regAccountField;
    private JTextField regBalanceField;
    private JButton addButton;
    private JLabel countLabel;

    // 조회 패널
    private JTextField searchAccountField;
    private JButton outputButton;
    private JTextField resultNameField;
    private JTextField resultBalanceField;

    public ViewImpl() {
        initUI();
    }

    private void initUI() {
        setTitle("계좌 관리 - Subject2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 320);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildRegisterPanel(), BorderLayout.NORTH);
        add(buildSearchPanel(), BorderLayout.CENTER);
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("고객 등록"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(new JLabel("이름"), gbc);
        regNameField = new JTextField(8);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(regNameField, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(new JLabel("계좌번호"), gbc);
        regAccountField = new JTextField(10);
        gbc.gridx = 3; gbc.weightx = 1;
        panel.add(regAccountField, gbc);

        gbc.gridx = 4; gbc.weightx = 0;
        panel.add(new JLabel("잔고"), gbc);
        regBalanceField = new JTextField(8);
        gbc.gridx = 5; gbc.weightx = 1;
        panel.add(regBalanceField, gbc);

        addButton = new JButton("추가");
        gbc.gridx = 6; gbc.weightx = 0;
        panel.add(addButton, gbc);

        countLabel = new JLabel("고객 수: 0명");
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 7; gbc.weightx = 1;
        panel.add(countLabel, gbc);

        return panel;
    }

    private JPanel buildSearchPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("계좌 조회"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(new JLabel("계좌번호"), gbc);
        searchAccountField = new JTextField(15);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(searchAccountField, gbc);
        outputButton = new JButton("출력");
        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(outputButton, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(new JLabel("이름"), gbc);
        resultNameField = new JTextField(15);
        resultNameField.setEditable(false);
        resultNameField.setBackground(Color.LIGHT_GRAY);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1;
        panel.add(resultNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0;
        panel.add(new JLabel("잔고"), gbc);
        resultBalanceField = new JTextField(15);
        resultBalanceField.setEditable(false);
        resultBalanceField.setBackground(Color.LIGHT_GRAY);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1;
        panel.add(resultBalanceField, gbc);

        return panel;
    }

    // ── AccountView 구현 ─────────────────────────────────────

    @Override public String getRegName()            { return regNameField.getText().trim(); }
    @Override public String getRegAccountNumber()   { return regAccountField.getText().trim(); }
    @Override public String getRegBalance()         { return regBalanceField.getText().trim(); }
    @Override public String getSearchAccountNumber(){ return searchAccountField.getText().trim(); }

    @Override
    public void setCount(int count) {
        countLabel.setText("고객 수: " + count + "명");
    }

    @Override
    public void showAccountResult(String name, long balance) {
        resultNameField.setText(name);
        resultBalanceField.setText(balance + "원");
    }

    @Override
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "입력 오류", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void clearRegistrationForm() {
        regNameField.setText("");
        regAccountField.setText("");
        regBalanceField.setText("");
    }

    @Override
    public void clearSearchResult() {
        resultNameField.setText("");
        resultBalanceField.setText("");
    }

    @Override
    public void bindAddListener(ActionListener listener) {
        addButton.addActionListener(listener);
    }

    @Override
    public void bindOutputListener(ActionListener listener) {
        outputButton.addActionListener(listener);
        searchAccountField.addActionListener(listener);
    }

    @Override
    public void display() {
        setVisible(true);
    }
}
