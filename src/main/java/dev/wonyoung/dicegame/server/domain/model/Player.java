package dev.wonyoung.dicegame.server.domain.model;

/**
 * 서버가 관리하는 접속 사용자.
 *
 * <p>식별자는 불변이고 상태({@link PlayerStatus})만 게임 진행에 따라 바뀐다.</p>
 */
public class Player {

    private final String id;
    private PlayerStatus status;

    public Player(String id) {
        this.id = id;
        this.status = PlayerStatus.LOBBY;
    }

    public String getId() {
        return id;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }

    public boolean isInLobby() {
        return status == PlayerStatus.LOBBY;
    }
}
