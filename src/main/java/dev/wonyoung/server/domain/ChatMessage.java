package dev.wonyoung.server.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ChatMessage(
        String sender,
        String receiver,
        String content,
        LocalDateTime timestamp,
        MessageType type
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public enum MessageType { BROADCAST, WHISPER }

    public static ChatMessage broadcast(String sender, String content) {
        return new ChatMessage(sender, null, content, LocalDateTime.now(), MessageType.BROADCAST);
    }

    public static ChatMessage whisper(String sender, String receiver, String content) {
        return new ChatMessage(sender, receiver, content, LocalDateTime.now(), MessageType.WHISPER);
    }

    public String formatted() {
        String time = "[" + timestamp.format(FORMATTER) + "] ";
        if (type == MessageType.WHISPER) {
            return time + "(귓속말) " + sender + " -> " + receiver + ": " + content;
        }
        return time + sender + ": " + content;
    }
}
