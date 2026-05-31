package dev.wonyoung.server.application.port.out;

public interface ChatSendPort {
    boolean register(String nickname, ClientSession session);
    void unregister(String nickname);
    void broadcast(String message);
    void whisper(String message, String senderNickname, String targetNickname);
    void closeAll();
}
