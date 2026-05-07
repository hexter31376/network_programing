package dev.wonyoung.adapter.in.swing.week6;

import dev.wonyoung.domain.model.Account;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AccountServerView extends JFrame {

    private final DefaultTableModel tableModel;
    private final JTextArea logArea = new JTextArea();

    public AccountServerView() {
        super("계좌 관리 서버 (port 7000)");

        String[] columns = {"계좌번호", "이름", "잔고"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        table.setRowHeight(22);

        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JLabel tableLabel = new JLabel("  전체 계좌 현황 (실시간)");
        tableLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(tableLabel, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                tablePanel, new JScrollPane(logArea));
        splitPane.setDividerLocation(220);

        add(splitPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 550);
        setLocationByPlatform(true);
        setVisible(true);
    }

    public void updateAccounts(List<Account> accounts) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (Account a : accounts) {
                tableModel.addRow(new Object[]{a.accountNumber(), a.name(), a.balance()});
            }
        });
    }

    public void appendLog(String text) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(text);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}
