package dev.wonyoung.chatting.client.application.port.out;

import dev.wonyoung.chatting.share.dto.ProtocolMessage;

public interface ServerPort {
    ProtocolMessage sendLogin(String userId) throws Exception;
    ProtocolMessage sendLogout(String userId) throws Exception;
    void close();
}
