package dev.wonyoung.dicegame.server.domain.port.out;

import dev.wonyoung.dicegame.protocol.Message;

/**
 * 특정 클라이언트(하나의 연결)로 메시지를 푸시하는 아웃 포트.
 *
 * <p>구현체는 연결마다 하나씩 생성되어 그 소켓의 출력 스트림에 JSON을 쓴다.
 * 서비스는 {@link PlayerRegistry}에서 대상 사용자의 notifier를 얻어 푸시한다.</p>
 */
public interface ClientNotifier {

    /**
     * 메시지 하나를 해당 클라이언트로 전송한다.
     *
     * @param message 보낼 메시지
     */
    void push(Message message);
}
