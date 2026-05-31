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

/**
 * 접속 중인 클라이언트 세션을 관리하고 메시지를 전송하는 출력 어댑터다.
 *
 * ConcurrentHashMap으로 닉네임과 세션을 관리해 스레드 안전성을 보장한다.
 * 닉네임 중복 등록은 putIfAbsent로 원자적으로 거부한다.
 * broadcast는 현재 접속 중인 모든 세션에게 메시지를 전송하고,
 * whisper는 발신자와 수신자 양쪽 모두에게 동일한 메시지를 전송한다.
 * closeAll은 서버 종료 시 세션 맵을 먼저 비운 뒤 연결을 종료해
 * 종료 과정에서 발생하는 재귀적 broadcast를 방지한다.
 */
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
