package dev.wonyoung.chatting.server.application.service;

import dev.wonyoung.chatting.server.application.port.in.LoginUseCase;
import dev.wonyoung.chatting.server.application.port.in.LogoutUseCase;
import dev.wonyoung.chatting.server.application.port.out.ServerEventPort;
import dev.wonyoung.chatting.server.application.port.out.UserRepository;
import dev.wonyoung.chatting.share.dto.ProtocolMessage;
import dev.wonyoung.common.container.di.Component;
import dev.wonyoung.common.container.di.Inject;

@Component
public class UserService implements LoginUseCase, LogoutUseCase {

    private static final String MULTICAST_ADDRESS = "230.0.0.1";
    private static final int MULTICAST_PORT = 4446;

    private final UserRepository userRepository;
    private final ServerEventPort eventPort;

    @Inject
    public UserService(UserRepository userRepository, ServerEventPort eventPort) {
        this.userRepository = userRepository;
        this.eventPort = eventPort;
    }

    @Override
    public ProtocolMessage login(String userId) {
        if (!userRepository.add(userId)) {
            return ProtocolMessage.loginFail("DUPLICATE_ID");
        }
        eventPort.onUserLoggedIn(userId);
        return ProtocolMessage.loginSuccess(MULTICAST_ADDRESS, MULTICAST_PORT);
    }

    @Override
    public void logout(String userId) {
        userRepository.remove(userId);
        eventPort.onUserLoggedOut(userId);
    }
}
