package dev.wonyoung.server.application.port.in;

/**
 * 채팅 메시지 전송 유스케이스 입력 포트다.
 *
 * 소켓 어댑터가 클라이언트로부터 메시지를 수신했을 때 호출하는 계약이다.
 * 전체 브로드캐스트와 특정 대상에게만 전송하는 귓속말 두 가지 방식을 정의한다.
 */
public interface ChatUseCase {
    void sendAllClients(String senderNickname, String message);
    void sendOneClient(String senderNickname, String message, String targetNickname);
}
