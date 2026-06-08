package dev.wonyoung.dicegame.server.adapter.out;

import dev.wonyoung.common.container.di.Component;
import dev.wonyoung.dicegame.server.domain.model.GameSession;
import dev.wonyoung.dicegame.server.domain.port.out.GameRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 메모리 기반 {@link GameRepository} 구현.
 */
@Component
public class InMemoryGameRepository implements GameRepository {

    private final Map<String, GameSession> games = new ConcurrentHashMap<>();

    @Override
    public void save(GameSession session) {
        games.put(session.getGameId(), session);
    }

    @Override
    public GameSession find(String gameId) {
        return games.get(gameId);
    }

    @Override
    public GameSession findByPlayer(String playerId) {
        return games.values().stream()
                .filter(session -> session.isParticipant(playerId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void remove(String gameId) {
        games.remove(gameId);
    }
}
