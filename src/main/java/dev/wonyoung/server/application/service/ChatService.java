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

/**
 * 채팅 서버의 핵심 도메인 서비스다.
 *
 * LoginUseCase, LogoutUseCase, ChatUseCase 세 가지 유스케이스를 한 클래스에서 구현한다.
 * 로그인 시 닉네임 중복 검사를 거쳐 세션을 등록하고 전체에 입장 알림을 보낸다.
 * 로그아웃 시 세션을 해제하고 퇴장 알림을 전체에 보낸다.
 * 채팅 메시지는 ChatMessage 도메인 객체로 포맷을 통일한 뒤 ChatSendPort를 통해 전송한다.
 */
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
