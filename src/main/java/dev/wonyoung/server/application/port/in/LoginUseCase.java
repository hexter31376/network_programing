package dev.wonyoung.server.application.port.in;

import dev.wonyoung.server.application.port.out.ClientSession;

public interface LoginUseCase {
    boolean login(ClientSession session, String nickname);
}
