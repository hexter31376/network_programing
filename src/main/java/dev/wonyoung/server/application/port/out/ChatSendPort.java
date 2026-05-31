package dev.wonyoung.server.application.port.out;

/**
 * 메시지 전송 출력 포트 인터페이스다.
 *
 * ChatService가 클라이언트에게 메시지를 전달할 때 사용하는 포트다.
 * 실제 전송 방식은 ClientSessionStore가 구현하며, 도메인은 전송 방식을 알지 못한다.
 * 전체 브로드캐스트, 귓속말, 세션 등록 및 해제, 서버 종료 시 전체 연결 종료 기능을 제공한다.
 */
public interface ChatSendPort {
    boolean register(String nickname, ClientSession session);
    void unregister(String nickname);
    void broadcast(String message);
    void whisper(String message, String senderNickname, String targetNickname);
    void closeAll();
}
