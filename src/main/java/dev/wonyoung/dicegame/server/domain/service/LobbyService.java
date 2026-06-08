package dev.wonyoung.dicegame.server.domain.service;

import dev.wonyoung.common.container.di.Component;
import dev.wonyoung.common.container.di.Inject;
import dev.wonyoung.dicegame.protocol.Message;
import dev.wonyoung.dicegame.protocol.MessageType;
import dev.wonyoung.dicegame.protocol.codec.MessageCodec;
import dev.wonyoung.dicegame.protocol.dto.GameEndedPayload;
import dev.wonyoung.dicegame.protocol.dto.UserListPayload;
import dev.wonyoung.dicegame.server.domain.model.GameScore;
import dev.wonyoung.dicegame.server.domain.model.GameSession;
import dev.wonyoung.dicegame.server.domain.model.Player;
import dev.wonyoung.dicegame.server.domain.model.PlayerStatus;
import dev.wonyoung.dicegame.server.domain.port.in.ListUsersUseCase;
import dev.wonyoung.dicegame.server.domain.port.in.LogoutUseCase;
import dev.wonyoung.dicegame.server.domain.port.out.ClientNotifier;
import dev.wonyoung.dicegame.server.domain.port.out.GameRepository;
import dev.wonyoung.dicegame.server.domain.port.out.PlayerRegistry;

/**
 * 로비 관련 서비스: 접속자 목록 제공과 로그아웃 처리.
 *
 * <p>로그아웃 시 게임 중이었다면 상대에게 게임 종료를 통지하고 상대를 로비로 되돌린다.</p>
 */
@Component
public class LobbyService implements ListUsersUseCase, LogoutUseCase {

    private final PlayerRegistry registry;
    private final GameRepository gameRepository;
    private final MessageCodec codec;

    @Inject
    public LobbyService(PlayerRegistry registry, GameRepository gameRepository, MessageCodec codec) {
        this.registry = registry;
        this.gameRepository = gameRepository;
        this.codec = codec;
    }

    @Override
    public void listUsers(String userId) {
        ClientNotifier notifier = registry.notifier(userId);
        if (notifier != null) {
            notifier.push(codec.message(MessageType.USER_LIST, new UserListPayload(registry.onlineUserIds())));
        }
    }

    @Override
    public void logout(String userId) {
        if (userId == null || !registry.exists(userId)) {
            return;
        }

        GameSession session = gameRepository.findByPlayer(userId);
        if (session != null) {
            abandonGame(session, userId);
        }

        registry.unregister(userId);
        broadcastUserList();
    }

    /**
     * 게임 중 한쪽이 나갔을 때, 남은 상대에게 최종 결과를 통지하고 로비로 되돌린 뒤 게임을 제거한다.
     */
    private void abandonGame(GameSession session, String leaverId) {
        String opponentId = session.opponentOf(leaverId);
        ClientNotifier opponentNotifier = registry.notifier(opponentId);
        if (opponentNotifier != null) {
            GameScore score = session.scoreOf(opponentId);
            opponentNotifier.push(codec.message(MessageType.GAME_ENDED, new GameEndedPayload(
                    session.getGameId(), score.wins(), score.losses(), score.draws(),
                    score.finalOutcome().name())));
        }
        Player opponent = registry.find(opponentId);
        if (opponent != null) {
            opponent.setStatus(PlayerStatus.LOBBY);
        }
        gameRepository.remove(session.getGameId());
    }

    /**
     * 현재 접속자 전원에게 갱신된 목록을 푸시한다.
     */
    private void broadcastUserList() {
        Message message = codec.message(MessageType.USER_LIST, new UserListPayload(registry.onlineUserIds()));
        for (String id : registry.onlineUserIds()) {
            ClientNotifier notifier = registry.notifier(id);
            if (notifier != null) {
                notifier.push(message);
            }
        }
    }
}
