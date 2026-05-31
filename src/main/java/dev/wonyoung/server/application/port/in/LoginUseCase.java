package dev.wonyoung.server.application.port.in;

import dev.wonyoung.server.application.port.out.ClientSession;

/**
 * 클라이언트 로그인 유스케이스 입력 포트다.
 *
 * 소켓 어댑터가 클라이언트로부터 첫 번째 줄인 닉네임을 수신했을 때 호출한다.
 * 중복 닉네임을 검사하고 세션을 등록한 뒤, 입장 알림을 전체에 브로드캐스트한다.
 * 중복 닉네임이면 false를 반환하고, 어댑터는 연결을 종료해야 한다.
 */
public interface LoginUseCase {
    boolean login(ClientSession session, String nickname);
}
