package dev.wonyoung.server.adapter.out.broadcast;

import dev.wonyoung.server.application.port.out.ChatSendPort;
import dev.wonyoung.server.application.port.out.ClientSession;
import dev.wonyoung.common.container.di.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ClientSessionStore implements ChatSendPort {

    private static final Logger logger = LoggerFactory.getLogger(ClientSessionStore.class);

    private final ConcurrentHashMap<String, ClientSession> sessions = new ConcurrentHashMap<>();

    @Override
    public boolean register(String nickname, ClientSession session) {
        if (sessions.putIfAbsent(nickname, session) != null) {
            logger.warn("닉네임 중복 거부: {}", nickname);
            return false;
        }
        logger.info("등록: {}", nickname);
        return true;
    }

    @Override
    public void unregister(String nickname) {
        sessions.remove(nickname);
        logger.info("해제: {}", nickname);
    }

    @Override
    public void broadcast(String message) {
        for (ClientSession session : sessions.values()) {
            trySend(session, message);
        }
    }

    @Override
    public void whisper(String message, String senderNickname, String targetNickname) {
        ClientSession target = sessions.get(targetNickname);
        ClientSession sender = sessions.get(senderNickname);

        if (target == null) {
            if (sender != null) trySend(sender, "대상 없음: " + targetNickname);
            return;
        }
        trySend(target, message);
        trySend(sender, message);
    }

    @Override
    public void closeAll() {
        List<ClientSession> snapshot = new ArrayList<>(sessions.values());
        // 맵을 먼저 비워서 close() 내 logout -> broadcast가 빈 세션 목록에 도달하도록 함
        sessions.clear();
        for (ClientSession session : snapshot) {
            try { session.send("서버가 종료됩니다."); } catch (IOException ignored) {}
            session.close();
        }
        logger.info("모든 클라이언트 연결 종료 ({}개)", snapshot.size());
    }

    private void trySend(ClientSession session, String message) {
        try {
            session.send(message);
        } catch (IOException e) {
            logger.error("전송 실패: {}", session.getId(), e);
        }
    }
}
