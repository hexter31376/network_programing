package dev.wonyoung.server.application.port.in;

/**
 * 클라이언트 로그아웃 유스케이스 입력 포트다.
 *
 * 클라이언트 연결이 끊어졌을 때 세션을 해제하고 퇴장 알림을 브로드캐스트한다.
 * shutdownAll은 서버 종료 시 모든 클라이언트 연결을 한 번에 정리할 때 사용한다.
 */
public interface LogoutUseCase {
    void logout(String nickname);
    void shutdownAll();
}
