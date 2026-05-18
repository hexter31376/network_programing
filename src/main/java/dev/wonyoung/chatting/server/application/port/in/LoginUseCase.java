package dev.wonyoung.chatting.server.application.port.in;

import dev.wonyoung.chatting.share.dto.ProtocolMessage;

public interface LoginUseCase {
    ProtocolMessage login(String userId);
}
