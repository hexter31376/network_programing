package dev.wonyoung.server.application.port.in;

public interface ChatUseCase {
    void sendAllClients(String senderNickname, String message);
    void sendOneClient(String senderNickname, String message, String targetNickname);
}
