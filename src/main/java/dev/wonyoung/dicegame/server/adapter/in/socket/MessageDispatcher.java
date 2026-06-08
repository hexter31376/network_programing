package dev.wonyoung.dicegame.server.adapter.in.socket;

import dev.wonyoung.common.container.di.Component;
import dev.wonyoung.common.container.di.Inject;
import dev.wonyoung.dicegame.protocol.Message;
import dev.wonyoung.dicegame.protocol.codec.MessageCodec;
import dev.wonyoung.dicegame.protocol.dto.GameIdPayload;
import dev.wonyoung.dicegame.protocol.dto.LoginPayload;
import dev.wonyoung.dicegame.protocol.dto.RespondGamePayload;
import dev.wonyoung.dicegame.protocol.dto.RollResultPayload;
import dev.wonyoung.dicegame.protocol.dto.TargetPayload;
import dev.wonyoung.dicegame.server.domain.exception.GameException;
import dev.wonyoung.dicegame.server.domain.port.in.EndGameUseCase;
import dev.wonyoung.dicegame.server.domain.port.in.ListUsersUseCase;
import dev.wonyoung.dicegame.server.domain.port.in.LoginUseCase;
import dev.wonyoung.dicegame.server.domain.port.in.LogoutUseCase;
import dev.wonyoung.dicegame.server.domain.port.in.RequestGameUseCase;
import dev.wonyoung.dicegame.server.domain.port.in.RespondGameUseCase;
import dev.wonyoung.dicegame.server.domain.port.in.SubmitRollUseCase;

/**
 * 들어온 메시지를 종류에 따라 알맞은 유스케이스로 라우팅하는 인바운드 어댑터.
 *
 * <p>각 연결의 {@link ClientSession}으로 현재 로그온 사용자 ID를 추적한다.
 * 알 수 없는 메시지는 {@link GameException}을 던지며, 이는 ExceptionHandler AOP가 로깅한다.</p>
 */
@Component
public class MessageDispatcher {

    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final RequestGameUseCase requestGameUseCase;
    private final RespondGameUseCase respondGameUseCase;
    private final SubmitRollUseCase submitRollUseCase;
    private final EndGameUseCase endGameUseCase;
    private final MessageCodec codec;

    @Inject
    public MessageDispatcher(LoginUseCase loginUseCase,
                             LogoutUseCase logoutUseCase,
                             ListUsersUseCase listUsersUseCase,
                             RequestGameUseCase requestGameUseCase,
                             RespondGameUseCase respondGameUseCase,
                             SubmitRollUseCase submitRollUseCase,
                             EndGameUseCase endGameUseCase,
                             MessageCodec codec) {
        this.loginUseCase = loginUseCase;
        this.logoutUseCase = logoutUseCase;
        this.listUsersUseCase = listUsersUseCase;
        this.requestGameUseCase = requestGameUseCase;
        this.respondGameUseCase = respondGameUseCase;
        this.submitRollUseCase = submitRollUseCase;
        this.endGameUseCase = endGameUseCase;
        this.codec = codec;
    }

    /**
     * 메시지 하나를 처리한다.
     *
     * @param message 수신한 메시지
     * @param session 이 연결의 세션
     */
    public void dispatch(Message message, ClientSession session) {
        switch (message.type()) {
            case LOGIN -> {
                LoginPayload payload = codec.payloadAs(message, LoginPayload.class);
                String userId = loginUseCase.login(payload.userId(), session.getNotifier());
                if (userId != null) {
                    session.setUserId(userId);
                }
            }
            case LOGOUT -> {
                logoutUseCase.logout(session.getUserId());
                session.setUserId(null);
            }
            case LIST_USERS -> listUsersUseCase.listUsers(session.getUserId());
            case REQUEST_GAME -> {
                TargetPayload payload = codec.payloadAs(message, TargetPayload.class);
                requestGameUseCase.requestGame(session.getUserId(), payload.targetId());
            }
            case RESPOND_GAME -> {
                RespondGamePayload payload = codec.payloadAs(message, RespondGamePayload.class);
                respondGameUseCase.respondGame(session.getUserId(), payload.requesterId(), payload.accept());
            }
            case ROLL_RESULT -> {
                RollResultPayload payload = codec.payloadAs(message, RollResultPayload.class);
                submitRollUseCase.submitRoll(session.getUserId(), payload.gameId(), payload.sum());
            }
            case END_GAME -> {
                GameIdPayload payload = codec.payloadAs(message, GameIdPayload.class);
                endGameUseCase.endGame(session.getUserId(), payload.gameId());
            }
            default -> throw new GameException("처리할 수 없는 메시지: " + message.type());
        }
    }

    /**
     * 연결 종료 시 정리. 로그온 상태였다면 로그아웃 처리한다.
     *
     * @param session 종료된 연결의 세션
     */
    public void disconnect(ClientSession session) {
        if (session.isLoggedIn()) {
            logoutUseCase.logout(session.getUserId());
            session.setUserId(null);
        }
    }
}
