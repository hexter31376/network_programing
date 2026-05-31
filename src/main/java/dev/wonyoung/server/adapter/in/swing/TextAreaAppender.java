package dev.wonyoung.server.adapter.in.swing;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Logback 로그 이벤트를 Swing JTextArea에 출력하는 커스텀 어펜더다.
 *
 * AppenderBase를 상속해 append 메서드에서 포맷된 로그 메시지를 받아
 * EDT에서 안전하게 JTextArea에 추가한다.
 * 서버 Application 시작 시 이 어펜더를 Logback에 등록해 서버 로그가 Swing 뷰에 보이게 한다.
 */
public class TextAreaAppender extends AppenderBase<ILoggingEvent> {

    private final JTextArea textArea;

    public TextAreaAppender(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    protected void append(ILoggingEvent event) {
        String line = event.getFormattedMessage() + "\n";
        SwingUtilities.invokeLater(() -> {
            textArea.append(line);
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }
}
