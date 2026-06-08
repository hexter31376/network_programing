package dev.wonyoung.dicegame.client.domain.port.out;

import dev.wonyoung.dicegame.protocol.Message;

import java.util.function.Consumer;

/**
 * 서버와의 양방향 메시지 통로(아웃 포트).
 *
 * <p>요청 전송과 서버 푸시 수신을 모두 담당한다. 수신한 메시지는
 * {@link #setMessageListener(Consumer)}로 등록한 리스너로 전달된다.</p>
 */
public interface ServerGateway {

    /**
     * 서버에 접속한다. 실패해도 예외를 던지지 않고 {@code false}를 반환한다
     * (AOP 예외 처리에 흡수되지 않도록 결과를 반환값으로 표현).
     *
     * @param host 서버 호스트
     * @param port 서버 포트
     * @return 접속 성공 여부
     */
    boolean connect(String host, int port);

    /**
     * 메시지를 서버로 전송한다.
     *
     * @param message 보낼 메시지
     */
    void send(Message message);

    /**
     * 서버 푸시 수신 리스너를 등록한다. (접속 전에 등록해야 한다)
     *
     * @param listener 수신한 메시지를 처리할 콜백
     */
    void setMessageListener(Consumer<Message> listener);

    /** 연결을 종료한다. */
    void close();
}
