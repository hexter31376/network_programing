package dev.wonyoung.server.adapter.in.socket;

import java.io.IOException;

/**
 * 모든 서버 구현체가 따르는 공통 인터페이스다.
 *
 * 헥사고날 아키텍처에서 소켓 입력 어댑터의 최상위 계약 역할을 한다.
 * Application은 이 인터페이스만 알고, 실제 구현체인 Blocking, NIO, Async, Virtual 서버는
 * 외부에서 주입되므로 교체해도 도메인 로직을 수정할 필요가 없다.
 */
public interface ChatServer {
    void start() throws IOException;
    void stop();
}
