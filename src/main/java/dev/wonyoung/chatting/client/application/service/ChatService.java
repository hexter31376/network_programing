package dev.wonyoung.chatting.client.application.service;

import dev.wonyoung.chatting.client.application.port.in.LoginUseCase;
import dev.wonyoung.chatting.client.application.port.in.LogoutUseCase;
import dev.wonyoung.chatting.client.application.port.in.SendMessageUseCase;
import dev.wonyoung.chatting.client.application.port.out.MulticastPort;
import dev.wonyoung.chatting.client.application.port.out.ServerPort;
import dev.wonyoung.chatting.share.domain.Protocol;
import dev.wonyoung.chatting.share.dto.ProtocolMessage;
import dev.wonyoung.common.container.di.Component;
import dev.wonyoung.common.container.di.Inject;

import java.util.function.Consumer;

@Component
public class ChatService implements LoginUseCase, LogoutUseCase, SendMessageUseCase {

    private final ServerPort serverPort;
    private final MulticastPort multicastPort;

    private volatile String currentUserId;

    @Inject
    public ChatService(ServerPort serverPort, MulticastPort multicastPort) {
        this.serverPort = serverPort;
        this.multicastPort = multicastPort;
    }

    @Override
    public boolean login(String userId) {
        try {
            ProtocolMessage response = serverPort.sendLogin(userId);
            if (response.type == Protocol.LOGIN_SUCCESS) {
                currentUserId = userId;
                multicastPort.joinGroup(response.multicastAddress, response.multicastPort);
                multicastPort.send(ProtocolMessage.join(userId));
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("[클라이언트] 로그인 실패: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void logout() {
        if (currentUserId == null) return;
        try {
            multicastPort.send(ProtocolMessage.leave(currentUserId));
            multicastPort.leaveGroup();
            serverPort.sendLogout(currentUserId);
            serverPort.close();
        } catch (Exception e) {
            System.err.println("[클라이언트] 로그아웃 오류: " + e.getMessage());
        } finally {
            currentUserId = null;
        }
    }

    @Override
    public void send(String message) {
        if (currentUserId == null) return;
        try {
            multicastPort.send(ProtocolMessage.chat(currentUserId, message));
        } catch (Exception e) {
            System.err.println("[클라이언트] 메시지 전송 실패: " + e.getMessage());
        }
    }

    public void startReceiving(Consumer<ProtocolMessage> handler) {
        multicastPort.startReceiving(handler);
    }

    public boolean isLoggedIn() {
        return currentUserId != null;
    }
}
