package dev.wonyoung.adapter.in.swing.week5;

import dev.wonyoung.infrastructure.container.di.Component;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.function.Consumer;

/**
 * Subject3 GUI: URL을 입력받아 전체 헤더, 텍스트/이미지 콘텐츠를 출력한다.
 * - 텍스트: JTextArea에 출력
 * - 이미지: JLabel에 출력
 * - 10일 이내 수정되지 않은 경우: 안내 메시지 출력
 */
@Component
public class Subject3View extends JFrame {

    private final JTextField urlField = new JTextField("https://google.com");
    private final JTextArea headersArea = new JTextArea();
    private final JTextArea textArea = new JTextArea();
    private final JLabel imageLabel = new JLabel("(이미지 없음)", SwingConstants.CENTER);

    public Subject3View() {
        super("페이지 뷰어 (Subject3)");
        headersArea.setEditable(false);
        textArea.setEditable(false);

        // 콘텐츠 영역: 텍스트 + 이미지를 세로로 배치
        var contentPanel = new JPanel(new GridLayout(2, 1));
        contentPanel.add(wrap("텍스트 본문", textArea));
        contentPanel.add(wrapLabel("이미지 본문", imageLabel));

        // 전체 레이아웃
        var centerPanel = new JPanel(new GridLayout(1, 2));
        centerPanel.add(wrap("응답 헤더", headersArea));
        centerPanel.add(contentPanel);

        add(urlField, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 700);
        setVisible(true);
    }

    public void setOnSubmit(Consumer<String> handler) {
        urlField.addActionListener(e -> handler.accept(urlField.getText().trim()));
    }

    public void showLoading() {
        headersArea.setText("로딩 중...");
        textArea.setText("");
        imageLabel.setIcon(null);
        imageLabel.setText("(이미지 없음)");
    }

    public void showHeaders(String text) {
        headersArea.setText(text);
    }

    public void showText(String text) {
        textArea.setText(text);
        imageLabel.setIcon(null);
        imageLabel.setText("");
    }

    public void showImage(ImageIcon icon) {
        imageLabel.setIcon(icon);
        imageLabel.setText("");
        textArea.setText("");
    }

    public void showMessage(String text) {
        textArea.setText(text);
        imageLabel.setIcon(null);
        imageLabel.setText("");
    }

    private static JScrollPane wrap(String title, JTextArea area) {
        var sp = new JScrollPane(area);
        sp.setBorder(BorderFactory.createTitledBorder(title));
        return sp;
    }

    private static JScrollPane wrapLabel(String title, JLabel label) {
        var sp = new JScrollPane(label);
        sp.setBorder(BorderFactory.createTitledBorder(title));
        return sp;
    }
}
