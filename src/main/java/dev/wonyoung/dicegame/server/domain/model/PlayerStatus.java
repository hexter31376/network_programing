package dev.wonyoung.dicegame.server.domain.model;

/**
 * 접속한 사용자의 현재 상태.
 */
public enum PlayerStatus {
    /** 로비에 있음(게임 신청·수락 가능) */
    LOBBY,
    /** 게임 진행 중 */
    IN_GAME
}
