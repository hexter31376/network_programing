package dev.wonyoung.adapter.in.swing.week5;

import dev.wonyoung.infrastructure.container.di.Component;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.function.Consumer;

/**
 * Subject1 GUI: URL을 입력받아 URL 메타 정보와 응답 본문을 출력한다.
 * <p>
 * - 상단: URL 입력 필드 (Enter 키로 제출)
 * - 중단 상: URL 메타 정보 (프로토콜, 호스트, 포트 등)
 * - 중단 하: 응답 본문 (텍스트) 또는 콘텐츠 정보
 * </p>
 */
@Component
public class Subject1View extends JFrame {

    private final JTextField urlField = new JTextField("https://google.com");
    private final JTextArea metaArea = new JTextArea();
    private final JTextArea bodyArea = new JTextArea();

    public Subject1View() {
        super("호스트 파일 읽기");
        metaArea.setEditable(false);
        bodyArea.setEditable(false);

        var grid = new JPanel(new GridLayout(2, 1));
        grid.add(wrap("URL 메타 정보", metaArea));
        grid.add(wrap("응답 본문", bodyArea));

        add(urlField, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(720, 600);
        setVisible(true);
    }

    /** JTextArea를 제목 테두리가 있는 JScrollPane으로 감싼다. */
    private static JScrollPane wrap(String title, JTextArea area) {
        var sp = new JScrollPane(area);
        sp.setBorder(BorderFactory.createTitledBorder(title));
        return sp;
    }

    /** URL 입력 필드에 Enter 키 핸들러를 등록한다. */
    public void setOnSubmit(Consumer<String> handler) {
        urlField.addActionListener(e -> handler.accept(urlField.getText().trim()));
    }

    /** 메타 영역을 비우고 본문 영역에 로딩 메시지를 표시한다. */
    public void showLoading() {
        metaArea.setText("");
        bodyArea.setText("로딩 중...");
    }

    public void showMeta(String text) {
        metaArea.setText(text);
    }

    public void showBody(String text) {
        bodyArea.setText(text);
    }
}
