package dev.wonyoung.dicegame.client.domain.service;

import dev.wonyoung.common.container.di.Component;
import dev.wonyoung.common.container.di.Inject;
import dev.wonyoung.dicegame.client.domain.port.in.ClientGameUseCase;
import dev.wonyoung.dicegame.client.domain.port.out.ServerGateway;
import dev.wonyoung.dicegame.protocol.MessageType;
import dev.wonyoung.dicegame.protocol.codec.MessageCodec;
import dev.wonyoung.dicegame.protocol.dto.GameIdPayload;
import dev.wonyoung.dicegame.protocol.dto.LoginPayload;
import dev.wonyoung.dicegame.protocol.dto.RespondGamePayload;
import dev.wonyoung.dicegame.protocol.dto.RollResultPayload;
import dev.wonyoung.dicegame.protocol.dto.TargetPayload;

import java.util.Random;

/**
 * 클라이언트 유스케이스 구현. 사용자 행동을 프로토콜 메시지로 만들어 {@link ServerGateway}로 보낸다.
 *
 * <p>주사위는 클라이언트에서 굴려 결과 합을 서버로 전송한다(서버는 두 합을 비교만 한다).
 * 진행 상태는 UI가 보유하므로 이 서비스는 사실상 무상태다.</p>
 */
@Component
public class ClientGameService implements ClientGameUseCase {

    private final ServerGateway gateway;
    private final MessageCodec codec;
    private final Random random = new Random();

    @Inject
    public ClientGameService(ServerGateway gateway, MessageCodec codec) {
        this.gateway = gateway;
        this.codec = codec;
    }

    @Override
    public boolean connect(String host, int port) {
        return gateway.connect(host, port);
    }

    @Override
    public void login(String userId) {
        gateway.send(codec.message(MessageType.LOGIN, new LoginPayload(userId)));
    }

    @Override
    public void refreshUsers() {
        gateway.send(codec.message(MessageType.LIST_USERS, null));
    }

    @Override
    public void requestGame(String targetId) {
        gateway.send(codec.message(MessageType.REQUEST_GAME, new TargetPayload(targetId)));
    }

    @Override
    public void respondGame(String requesterId, boolean accept) {
        gateway.send(codec.message(MessageType.RESPOND_GAME, new RespondGamePayload(requesterId, accept)));
    }

    @Override
    public int[] rollDice(String gameId) {
        int first = random.nextInt(6) + 1;
        int second = random.nextInt(6) + 1;
        int[] dice = {first, second};
        gateway.send(codec.message(MessageType.ROLL_RESULT, new RollResultPayload(gameId, dice, first + second)));
        return dice;
    }

    @Override
    public void endGame(String gameId) {
        if (gameId != null) {
            gateway.send(codec.message(MessageType.END_GAME, new GameIdPayload(gameId)));
        }
    }

    @Override
    public void logout() {
        gateway.send(codec.message(MessageType.LOGOUT, null));
    }

    @Override
    public void disconnect() {
        gateway.close();
    }
}
