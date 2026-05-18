package dev.wonyoung.chatting.client.application.port.out;

import dev.wonyoung.chatting.share.dto.ProtocolMessage;

import java.util.function.Consumer;

public interface MulticastPort {
    void joinGroup(String address, int port) throws Exception;
    void send(ProtocolMessage message) throws Exception;
    void startReceiving(Consumer<ProtocolMessage> handler);
    void leaveGroup() throws Exception;
}
