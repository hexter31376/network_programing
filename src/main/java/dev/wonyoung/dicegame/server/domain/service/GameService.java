package dev.wonyoung.dicegame.server.domain.service;

import dev.wonyoung.common.container.di.Component;
import dev.wonyoung.common.container.di.Inject;
import dev.wonyoung.dicegame.protocol.MessageType;
import dev.wonyoung.dicegame.protocol.codec.MessageCodec;
import dev.wonyoung.dicegame.protocol.dto.ErrorPayload;
import dev.wonyoung.dicegame.protocol.dto.GameEndedPayload;
import dev.wonyoung.dicegame.protocol.dto.RoundResultPayload;
import dev.wonyoung.dicegame.server.domain.model.GameScore;
import dev.wonyoung.dicegame.server.domain.model.GameSession;
import dev.wonyoung.dicegame.server.domain.model.Player;
import dev.wonyoung.dicegame.server.domain.model.PlayerStatus;
import dev.wonyoung.dicegame.server.domain.model.RoundOutcome;
import dev.wonyoung.dicegame.server.domain.model.RoundResult;
import dev.wonyoung.dicegame.server.domain.port.in.EndGameUseCase;
import dev.wonyoung.dicegame.server.domain.port.in.SubmitRollUseCase;
import dev.wonyoung.dicegame.server.domain.port.out.ClientNotifier;
import dev.wonyoung.dicegame.server.domain.port.out.GameRepository;
import dev.wonyoung.dicegame.server.domain.port.out.PlayerRegistry;

/**
 * 게임 진행 서비스: 라운드 주사위 비교와 게임 종료를 처리한다.
 *
 * <p>두 플레이어의 주사위 합이 모두 도착하면 비교하여 양측에 각자 관점의 {@code ROUND_RESULT}를
 * 보낸다. 종료 시 누적 점수로 {@code GAME_ENDED}를 보내고 두 플레이어를 로비로 되돌린다.</p>
 */
@Component
public class GameService implements SubmitRollUseCase, EndGameUseCase {

    private final PlayerRegistry registry;
    private final GameRepository gameRepository;
    private final MessageCodec codec;

    @Inject
    public GameService(PlayerRegistry registry, GameRepository gameRepository, MessageCodec codec) {
        this.registry = registry;
        this.gameRepository = gameRepository;
        this.codec = codec;
    }

    @Override
    public void submitRoll(String playerId, String gameId, int sum) {
        GameSession session = gameRepository.find(gameId);
        if (session == null || !session.isParticipant(playerId)) {
            pushError(playerId, "NO_SUCH_GAME", "진행 중인 게임을 찾을 수 없습니다.");
            return;
        }

        boolean roundComplete = session.submitRoll(playerId, sum);
        if (!roundComplete) {
            return; // 상대의 제출을 기다린다
        }

        RoundResult result = session.resolveRound();
        pushRound(session.getPlayerA(), gameId, result.sumA(), result.sumB(), result.outcomeA());
        pushRound(session.getPlayerB(), gameId, result.sumB(), result.sumA(), result.outcomeA().invert());
    }

    @Override
    public void endGame(String playerId, String gameId) {
        GameSession session = gameRepository.find(gameId);
        if (session == null) {
            return; // 이미 종료됨(상대가 먼저 종료를 눌렀을 수 있음)
        }

        pushEnded(session.getPlayerA(), session);
        pushEnded(session.getPlayerB(), session);
        returnToLobby(session.getPlayerA());
        returnToLobby(session.getPlayerB());
        gameRepository.remove(gameId);
    }

    private void pushRound(String playerId, String gameId, int yourSum, int oppSum, RoundOutcome outcome) {
        ClientNotifier notifier = registry.notifier(playerId);
        if (notifier != null) {
            notifier.push(codec.message(MessageType.ROUND_RESULT,
                    new RoundResultPayload(gameId, yourSum, oppSum, outcome.name())));
        }
    }

    private void pushEnded(String playerId, GameSession session) {
        ClientNotifier notifier = registry.notifier(playerId);
        if (notifier != null) {
            GameScore score = session.scoreOf(playerId);
            notifier.push(codec.message(MessageType.GAME_ENDED, new GameEndedPayload(
                    session.getGameId(), score.wins(), score.losses(), score.draws(),
                    score.finalOutcome().name())));
        }
    }

    private void returnToLobby(String playerId) {
        Player player = registry.find(playerId);
        if (player != null) {
            player.setStatus(PlayerStatus.LOBBY);
        }
    }

    private void pushError(String playerId, String code, String message) {
        ClientNotifier notifier = registry.notifier(playerId);
        if (notifier != null) {
            notifier.push(codec.message(MessageType.ERROR, new ErrorPayload(code, message)));
        }
    }
}
