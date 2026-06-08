package dev.wonyoung.dicegame.server.adapter.in.socket;

import dev.wonyoung.dicegame.server.domain.port.out.ClientNotifier;

/**
 * 한 클라이언트 연결의 세션 상태.
 *
 * <p>해당 연결의 푸시 채널({@link ClientNotifier})과, 로그온 후 확정된 사용자 ID를 보관한다.
 * 연결당 하나씩 {@code ClientConnectionHandler}가 생성하여 {@code MessageDispatcher}에 넘긴다.</p>
 */
public class ClientSession {

    private final ClientNotifier notifier;
    private String userId;

    public ClientSession(ClientNotifier notifier) {
        this.notifier = notifier;
    }

    public ClientNotifier getNotifier() {
        return notifier;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isLoggedIn() {
        return userId != null;
    }
}
