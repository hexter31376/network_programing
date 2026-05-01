package dev.wonyoung.domin.model;

public record Message(
        String content,
        String clientAddress
) {

    public Message {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("메시지 내용은 비어있을 수 없습니다.");
        }
    }

    public Message echo() {
        return new Message(content, clientAddress);
    }
}
