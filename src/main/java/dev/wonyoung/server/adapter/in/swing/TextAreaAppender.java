package dev.wonyoung.server.adapter.in.swing;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

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
