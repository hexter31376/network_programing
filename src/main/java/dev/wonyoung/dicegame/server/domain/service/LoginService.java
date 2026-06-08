package dev.wonyoung.dicegame.server.domain.service;

import dev.wonyoung.common.container.di.Component;
import dev.wonyoung.common.container.di.Inject;
import dev.wonyoung.dicegame.protocol.Message;
import dev.wonyoung.dicegame.protocol.MessageType;
import dev.wonyoung.dicegame.protocol.codec.MessageCodec;
import dev.wonyoung.dicegame.protocol.dto.ErrorPayload;
import dev.wonyoung.dicegame.protocol.dto.LoginResultPayload;
import dev.wonyoung.dicegame.protocol.dto.UserListPayload;
import dev.wonyoung.dicegame.server.domain.model.Player;
import dev.wonyoung.dicegame.server.domain.port.in.LoginUseCase;
import dev.wonyoung.dicegame.server.domain.port.out.ClientNotifier;
import dev.wonyoung.dicegame.server.domain.port.out.PlayerRegistry;

/**
 * 로그온 처리 서비스.
 *
 * <p>ID 중복 검사 후 결과를 요청 클라이언트에 통지하고, 성공 시 다른 접속자에게
 * 갱신된 접속자 목록을 브로드캐스트한다. 중복 같은 비즈니스 결과는 예외가 아니라
 * 메시지로 알린다(예외는 ExceptionHandler AOP가 로깅하는 진짜 오류용).</p>
 */
@Component
public class LoginService implements LoginUseCase {

    private final PlayerRegistry registry;
    private final MessageCodec codec;

    @Inject
    public LoginService(PlayerRegistry registry, MessageCodec codec) {
        this.registry = registry;
        this.codec = codec;
    }

    @Override
    public String login(String userId, ClientNotifier notifier) {
        if (userId == null || userId.isBlank()) {
            notifier.push(codec.message(MessageType.ERROR,
                    new ErrorPayload("INVALID_ID", "ID가 비어 있습니다.")));
            return null;
        }

        boolean registered = registry.register(new Player(userId), notifier);
        if (!registered) {
            notifier.push(codec.message(MessageType.LOGIN_RESULT,
                    new LoginResultPayload(false, "이미 사용 중인 ID입니다: " + userId, registry.onlineUserIds())));
            return null;
        }

        notifier.push(codec.message(MessageType.LOGIN_RESULT,
                new LoginResultPayload(true, "", registry.onlineUserIds())));
        broadcastUserListExcept(userId);
        return userId;
    }

    /**
     * 방금 로그온한 사용자를 제외한 모든 접속자에게 갱신된 목록을 푸시한다.
     */
    private void broadcastUserListExcept(String exceptId) {
        Message message = codec.message(MessageType.USER_LIST, new UserListPayload(registry.onlineUserIds()));
        for (String id : registry.onlineUserIds()) {
            if (id.equals(exceptId)) {
                continue;
            }
            ClientNotifier other = registry.notifier(id);
            if (other != null) {
                other.push(message);
            }
        }
    }
}
