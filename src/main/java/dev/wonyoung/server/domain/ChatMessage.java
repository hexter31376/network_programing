package dev.wonyoung.server.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 채팅 메시지를 표현하는 도메인 레코드다.
 *
 * 전체 브로드캐스트와 귓속말 두 종류를 지원한다.
 * broadcast 팩토리 메서드는 receiver를 null로 두고, whisper는 receiver를 지정한다.
 * formatted 메서드가 시간을 포함한 최종 문자열을 만들어 반환하므로
 * 전송 직전에 이 메서드를 호출해 포맷을 통일한다.
 */
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
