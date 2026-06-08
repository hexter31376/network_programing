package dev.wonyoung.dicegame.server.domain.service;

import dev.wonyoung.common.container.di.Component;
import dev.wonyoung.common.container.di.Inject;
import dev.wonyoung.dicegame.protocol.MessageType;
import dev.wonyoung.dicegame.protocol.codec.MessageCodec;
import dev.wonyoung.dicegame.protocol.dto.ErrorPayload;
import dev.wonyoung.dicegame.protocol.dto.GameRequestedPayload;
import dev.wonyoung.dicegame.protocol.dto.GameStartedPayload;
import dev.wonyoung.dicegame.protocol.dto.TargetPayload;
import dev.wonyoung.dicegame.server.domain.model.GameSession;
import dev.wonyoung.dicegame.server.domain.model.Player;
import dev.wonyoung.dicegame.server.domain.model.PlayerStatus;
import dev.wonyoung.dicegame.server.domain.port.in.RequestGameUseCase;
import dev.wonyoung.dicegame.server.domain.port.in.RespondGameUseCase;
import dev.wonyoung.dicegame.server.domain.port.out.ClientNotifier;
import dev.wonyoung.dicegame.server.domain.port.out.GameRepository;
import dev.wonyoung.dicegame.server.domain.port.out.PlayerRegistry;

import java.util.UUID;

/**
 * 매칭 서비스: 게임 신청과 그 응답(수락/거절)을 처리한다.
 *
 * <p>상대가 게임 중이면 {@code GAME_BUSY}로 알려 다른 사용자를 고르게 하고,
 * 수락 시 게임을 만들어 양측을 {@code IN_GAME} 상태로 전환한다.</p>
 */
@Component
public class MatchmakingService implements RequestGameUseCase, RespondGameUseCase {

    private final PlayerRegistry registry;
    private final GameRepository gameRepository;
    private final MessageCodec codec;

    @Inject
    public MatchmakingService(PlayerRegistry registry, GameRepository gameRepository, MessageCodec codec) {
        this.registry = registry;
        this.gameRepository = gameRepository;
        this.codec = codec;
    }

    @Override
    public void requestGame(String requesterId, String targetId) {
        ClientNotifier requesterNotifier = registry.notifier(requesterId);
        if (requesterNotifier == null) {
            return;
        }

        if (targetId == null || targetId.equals(requesterId) || !registry.exists(targetId)) {
            requesterNotifier.push(codec.message(MessageType.ERROR,
                    new ErrorPayload("NO_SUCH_USER", "대상 사용자를 찾을 수 없습니다: " + targetId)));
            return;
        }

        Player target = registry.find(targetId);
        if (!target.isInLobby()) {
            requesterNotifier.push(codec.message(MessageType.GAME_BUSY, new TargetPayload(targetId)));
            return;
        }

        ClientNotifier targetNotifier = registry.notifier(targetId);
        if (targetNotifier != null) {
            targetNotifier.push(codec.message(MessageType.GAME_REQUESTED, new GameRequestedPayload(requesterId)));
        }
    }

    @Override
    public void respondGame(String responderId, String requesterId, boolean accept) {
        ClientNotifier responderNotifier = registry.notifier(responderId);
        ClientNotifier requesterNotifier = registry.notifier(requesterId);

        if (!accept) {
            if (requesterNotifier != null) {
                requesterNotifier.push(codec.message(MessageType.GAME_DECLINED, new TargetPayload(responderId)));
            }
            return;
        }

        Player requester = registry.find(requesterId);
        Player responder = registry.find(responderId);
        if (requester == null || responder == null) {
            if (responderNotifier != null) {
                responderNotifier.push(codec.message(MessageType.ERROR,
                        new ErrorPayload("NO_SUCH_USER", "상대를 찾을 수 없습니다: " + requesterId)));
            }
            return;
        }

        if (!requester.isInLobby() || !responder.isInLobby()) {
            if (responderNotifier != null) {
                responderNotifier.push(codec.message(MessageType.GAME_BUSY, new TargetPayload(requesterId)));
            }
            return;
        }

        String gameId = UUID.randomUUID().toString();
        gameRepository.save(new GameSession(gameId, requesterId, responderId));
        requester.setStatus(PlayerStatus.IN_GAME);
        responder.setStatus(PlayerStatus.IN_GAME);

        if (requesterNotifier != null) {
            requesterNotifier.push(codec.message(MessageType.GAME_STARTED, new GameStartedPayload(gameId, responderId)));
        }
        if (responderNotifier != null) {
            responderNotifier.push(codec.message(MessageType.GAME_STARTED, new GameStartedPayload(gameId, requesterId)));
        }
    }
}
