package dev.wonyoung.server.application.service;

import dev.wonyoung.server.application.port.in.ChatUseCase;
import dev.wonyoung.server.application.port.in.LoginUseCase;
import dev.wonyoung.server.application.port.in.LogoutUseCase;
import dev.wonyoung.server.application.port.out.ChatSendPort;
import dev.wonyoung.server.application.port.out.ClientSession;
import dev.wonyoung.server.domain.ChatMessage;
import dev.wonyoung.common.container.di.Component;
import dev.wonyoung.common.container.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ChatService implements LoginUseCase, LogoutUseCase, ChatUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatSendPort chatSendPort;

    @Inject
    public ChatService(ChatSendPort chatSendPort) {
        this.chatSendPort = chatSendPort;
    }

    @Override
    public boolean login(ClientSession session, String nickname) {
        if (!chatSendPort.register(nickname, session)) {
            return false;
        }
        String notice = nickname + "님이 입장했습니다.";
        logger.info(notice);
        chatSendPort.broadcast(notice);
        return true;
    }

    @Override
    public void logout(String nickname) {
        chatSendPort.unregister(nickname);
        String notice = nickname + "님이 퇴장했습니다.";
        logger.info(notice);
        chatSendPort.broadcast(notice);
    }

    @Override
    public void sendAllClients(String senderNickname, String message) {
        String formatted = ChatMessage.broadcast(senderNickname, message).formatted();
        logger.info(formatted);
        chatSendPort.broadcast(formatted);
    }

    @Override
    public void sendOneClient(String senderNickname, String message, String targetNickname) {
        String formatted = ChatMessage.whisper(senderNickname, targetNickname, message).formatted();
        logger.info(formatted);
        chatSendPort.whisper(formatted, senderNickname, targetNickname);
    }

    @Override
    public void shutdownAll() {
        chatSendPort.closeAll();
    }
}
