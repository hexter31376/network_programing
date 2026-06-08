package dev.wonyoung.dicegame.server.domain.port.out;

import dev.wonyoung.dicegame.server.domain.model.GameSession;

/**
 * 진행 중인 게임({@link GameSession})을 보관·조회하는 아웃 포트.
 */
public interface GameRepository {

    /**
     * 게임을 저장한다.
     *
     * @param session 저장할 게임
     */
    void save(GameSession session);

    /**
     * 게임 ID로 진행 중인 게임을 조회한다.
     *
     * @param gameId 게임 식별자
     * @return 게임, 없으면 {@code null}
     */
    GameSession find(String gameId);

    /**
     * 특정 플레이어가 참여 중인 게임을 조회한다(로그아웃·연결 종료 처리용).
     *
     * @param playerId 플레이어 ID
     * @return 참여 중인 게임, 없으면 {@code null}
     */
    GameSession findByPlayer(String playerId);

    /**
     * 게임을 제거한다(종료 시).
     *
     * @param gameId 게임 식별자
     */
    void remove(String gameId);
}
