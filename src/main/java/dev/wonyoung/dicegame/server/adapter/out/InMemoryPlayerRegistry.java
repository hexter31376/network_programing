package dev.wonyoung.dicegame.server.adapter.out;

import dev.wonyoung.common.container.di.Component;
import dev.wonyoung.dicegame.server.domain.model.Player;
import dev.wonyoung.dicegame.server.domain.port.out.ClientNotifier;
import dev.wonyoung.dicegame.server.domain.port.out.PlayerRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 메모리 기반 {@link PlayerRegistry} 구현.
 *
 * <p>{@link ConcurrentHashMap}으로 다중 접속 환경의 동시 접근을 안전하게 처리한다.
 * 등록은 {@code putIfAbsent}로 원자적으로 수행하여 ID 중복을 막는다.</p>
 */
@Component
public class InMemoryPlayerRegistry implements PlayerRegistry {

    /** 사용자 정보와 그 사용자의 푸시 채널을 함께 보관한다. */
    private record Entry(Player player, ClientNotifier notifier) {
    }

    private final Map<String, Entry> players = new ConcurrentHashMap<>();

    @Override
    public boolean register(Player player, ClientNotifier notifier) {
        return players.putIfAbsent(player.getId(), new Entry(player, notifier)) == null;
    }

    @Override
    public void unregister(String userId) {
        players.remove(userId);
    }

    @Override
    public boolean exists(String userId) {
        return players.containsKey(userId);
    }

    @Override
    public Player find(String userId) {
        Entry entry = players.get(userId);
        return entry == null ? null : entry.player();
    }

    @Override
    public ClientNotifier notifier(String userId) {
        Entry entry = players.get(userId);
        return entry == null ? null : entry.notifier();
    }

    @Override
    public List<String> onlineUserIds() {
        return new ArrayList<>(players.keySet());
    }
}
