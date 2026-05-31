package dev.wonyoung.server.application.port.out;

import java.io.IOException;

/**
 * 서버에 연결된 클라이언트 세션을 나타내는 출력 포트 인터페이스다.
 *
 * 소켓 종류인 일반 Socket, NIO SocketChannel, AIO AsynchronousSocketChannel에
 * 관계없이 동일한 방식으로 메시지를 보내거나 연결을 닫을 수 있도록 추상화한다.
 * ClientSessionStore가 이 인터페이스를 통해 세션을 관리한다.
 */
public interface ClientSession {
    String getId();
    void send(String message) throws IOException;
    void close();
}
