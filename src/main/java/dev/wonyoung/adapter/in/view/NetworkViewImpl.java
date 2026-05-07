package dev.wonyoung.adapter.in.view;

import dev.wonyoung.view.NetworkView;

import javax.swing.*;
import java.awt.*;

public class NetworkViewImpl implements NetworkView {

    private static final String TITLE           = "Network Utility";
    private static final String LABEL_HOST      = "Hostname / IP:";
    private static final String BTN_LOOKUP_IP   = "Lookup IP";
    private static final String BTN_LOOKUP_ALL  = "Lookup All IPs";
    private static final String BTN_PING        = "Ping Test";

    private static final int FRAME_WIDTH        = 600;
    private static final int FRAME_HEIGHT       = 450;
    private static final int HOST_FIELD_COLUMNS = 25;

    private final JFrame frame;
    private final JTextField hostField;
    private final JTextArea resultArea;

    public NetworkViewImpl(Runnable onLookupIp, Runnable onLookupAllIps, Runnable onPingTest) {
        frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setLocationRelativeTo(null);

        hostField = new JTextField(HOST_FIELD_COLUMNS);
        resultArea = new JTextArea();
        resultArea.setEditable(false);

        JButton ipButton = new JButton(BTN_LOOKUP_IP);
        JButton allIpButton = new JButton(BTN_LOOKUP_ALL);
        JButton pingButton = new JButton(BTN_PING);

        ipButton.addActionListener(e -> onLookupIp.run());
        allIpButton.addActionListener(e -> onLookupAllIps.run());
        pingButton.addActionListener(e -> onPingTest.run());

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel(LABEL_HOST));
        inputPanel.add(hostField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(ipButton);
        buttonPanel.add(allIpButton);
        buttonPanel.add(pingButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(resultArea), BorderLayout.CENTER);
    }

    @Override
    public String getHost() {
        return hostField.getText().trim();
    }

    @Override
    public void showResult(String text) {
        SwingUtilities.invokeLater(() -> resultArea.setText(text));
    }

    @Override
    public void show() {
        frame.setVisible(true);
    }
}
