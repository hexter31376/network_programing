package dev.wonyoung.adapter.in.swing.week5;

import dev.wonyoung.infrastructure.container.di.Component;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.function.Consumer;

/**
 * Subject2 GUI: URL을 입력받아 헤더 목록과 콘텐츠 분류 결과를 출력한다.
 */
@Component
public class Subject2View extends JFrame {

    private final JTextField urlField = new JTextField("https://google.com");
    private final JTextArea headersArea = new JTextArea();
    private final JTextField classifiedField = new JTextField();

    public Subject2View() {
        super("헤더 분석기 (Subject2)");
        headersArea.setEditable(false);
        classifiedField.setEditable(false);

        var bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(new JLabel("분류: "), BorderLayout.WEST);
        bottomPanel.add(classifiedField, BorderLayout.CENTER);

        var centerPanel = new JPanel(new GridLayout(1, 1));
        centerPanel.add(wrap("응답 헤더", headersArea));

        add(urlField, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(720, 600);
        setVisible(true);
    }

    public void setOnSubmit(Consumer<String> handler) {
        urlField.addActionListener(e -> handler.accept(urlField.getText().trim()));
    }

    public void showLoading() {
        headersArea.setText("로딩 중...");
        classifiedField.setText("");
    }

    public void showHeaders(String text) {
        headersArea.setText(text);
    }

    public void showClassified(String label) {
        classifiedField.setText(label);
    }

    private static JScrollPane wrap(String title, JTextArea area) {
        var sp = new JScrollPane(area);
        sp.setBorder(BorderFactory.createTitledBorder(title));
        return sp;
    }
}
