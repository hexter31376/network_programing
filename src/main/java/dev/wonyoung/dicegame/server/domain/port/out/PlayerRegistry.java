package dev.wonyoung.dicegame.server.domain.port.out;

import dev.wonyoung.dicegame.server.domain.model.Player;

import java.util.List;

/**
 * 접속한 사용자와 각 사용자의 푸시 채널({@link ClientNotifier})을 보관·조회하는 아웃 포트.
 *
 * <p>다중 접속 환경에서 여러 워커 스레드가 동시에 접근하므로 구현체는 스레드 안전해야 한다.</p>
 */
public interface PlayerRegistry {

    /**
     * 새 사용자를 등록한다. ID가 이미 존재하면 등록하지 않는다.
     *
     * @param player   등록할 사용자
     * @param notifier 이 사용자에게 푸시할 채널
     * @return 등록 성공 시 {@code true}, ID 중복이면 {@code false}
     */
    boolean register(Player player, ClientNotifier notifier);

    /**
     * 사용자를 등록 해제한다(로그아웃/연결 종료).
     *
     * @param userId 해제할 사용자 ID
     */
    void unregister(String userId);

    /**
     * 해당 ID의 사용자가 접속 중인지 여부.
     *
     * @param userId 검사할 ID
     * @return 존재하면 {@code true}
     */
    boolean exists(String userId);

    /**
     * 사용자 정보를 조회한다.
     *
     * @param userId 조회할 ID
     * @return 사용자, 없으면 {@code null}
     */
    Player find(String userId);

    /**
     * 사용자의 푸시 채널을 조회한다.
     *
     * @param userId 조회할 ID
     * @return 해당 사용자의 notifier, 없으면 {@code null}
     */
    ClientNotifier notifier(String userId);

    /**
     * 현재 접속 중인 모든 사용자 ID 목록.
     *
     * @return 사용자 ID 목록
     */
    List<String> onlineUserIds();
}
