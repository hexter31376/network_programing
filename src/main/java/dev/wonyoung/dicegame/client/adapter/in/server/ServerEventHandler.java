package dev.wonyoung.dicegame.client.adapter.in.server;

import dev.wonyoung.common.container.di.Component;
import dev.wonyoung.common.container.di.Inject;
import dev.wonyoung.dicegame.client.domain.port.out.GameEventPort;
import dev.wonyoung.dicegame.protocol.Message;
import dev.wonyoung.dicegame.protocol.codec.MessageCodec;
import dev.wonyoung.dicegame.protocol.dto.ErrorPayload;
import dev.wonyoung.dicegame.protocol.dto.GameEndedPayload;
import dev.wonyoung.dicegame.protocol.dto.GameRequestedPayload;
import dev.wonyoung.dicegame.protocol.dto.GameStartedPayload;
import dev.wonyoung.dicegame.protocol.dto.LoginResultPayload;
import dev.wonyoung.dicegame.protocol.dto.RoundResultPayload;
import dev.wonyoung.dicegame.protocol.dto.TargetPayload;
import dev.wonyoung.dicegame.protocol.dto.UserListPayload;

/**
 * 서버 푸시 메시지를 해석하여 UI({@link GameEventPort})로 전달하는 인바운드 어댑터.
 *
 * <p>{@code TcpServerGateway}의 리스너로 등록되어 수신 스레드에서 호출된다.
 * 실제 화면 갱신은 {@link GameEventPort} 구현(MainFrame)이 EDT에서 수행한다.</p>
 */
@Component
public class ServerEventHandler {

    private final MessageCodec codec;
    private GameEventPort eventPort;

    @Inject
    public ServerEventHandler(MessageCodec codec) {
        this.codec = codec;
    }

    /**
     * UI 이벤트 포트를 연결한다. (앱 시작 시 1회)
     *
     * @param eventPort UI 구현
     */
    public void bindEventPort(GameEventPort eventPort) {
        this.eventPort = eventPort;
    }

    /**
     * 수신한 서버 메시지를 처리한다.
     *
     * @param message 서버가 보낸 메시지
     */
    public void handle(Message message) {
        if (eventPort == null) {
            return;
        }
        switch (message.type()) {
            case LOGIN_RESULT -> {
                LoginResultPayload payload = codec.payloadAs(message, LoginResultPayload.class);
                eventPort.onLoginResult(payload.success(), payload.reason(), payload.users());
            }
            case USER_LIST -> {
                UserListPayload payload = codec.payloadAs(message, UserListPayload.class);
                eventPort.onUserList(payload.users());
            }
            case GAME_REQUESTED -> {
                GameRequestedPayload payload = codec.payloadAs(message, GameRequestedPayload.class);
                eventPort.onGameRequested(payload.fromId());
            }
            case GAME_BUSY -> {
                TargetPayload payload = codec.payloadAs(message, TargetPayload.class);
                eventPort.onGameBusy(payload.targetId());
            }
            case GAME_DECLINED -> {
                TargetPayload payload = codec.payloadAs(message, TargetPayload.class);
                eventPort.onGameDeclined(payload.targetId());
            }
            case GAME_STARTED -> {
                GameStartedPayload payload = codec.payloadAs(message, GameStartedPayload.class);
                eventPort.onGameStarted(payload.gameId(), payload.opponentId());
            }
            case ROUND_RESULT -> {
                RoundResultPayload payload = codec.payloadAs(message, RoundResultPayload.class);
                eventPort.onRoundResult(payload.yourSum(), payload.oppSum(), payload.outcome());
            }
            case GAME_ENDED -> {
                GameEndedPayload payload = codec.payloadAs(message, GameEndedPayload.class);
                eventPort.onGameEnded(payload.wins(), payload.losses(), payload.draws(), payload.finalOutcome());
            }
            case ERROR -> {
                ErrorPayload payload = codec.payloadAs(message, ErrorPayload.class);
                eventPort.onError(payload.code(), payload.message());
            }
            default -> {
                // 클라이언트는 처리하지 않는 메시지 타입(요청용)일 수 있다. 무시한다.
            }
        }
    }
}
