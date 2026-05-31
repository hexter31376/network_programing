package dev.wonyoung.server.adapter.in.socket.nonblocking;

import dev.wonyoung.server.application.port.in.ChatUseCase;
import dev.wonyoung.server.application.port.in.LoginUseCase;
import dev.wonyoung.server.application.port.in.LogoutUseCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * NIO 논블로킹 서버에서 클라이언트 연결 하나의 상태를 관리하는 세션 클래스다.
 *
 * TCP는 스트림 기반이므로 한 번의 read에 여러 줄이 올 수 있다.
 * ByteArrayOutputStream을 줄 버퍼로 사용해 바이트를 누적하다가
 * 줄바꿈 문자를 만나면 완성된 줄을 processLine으로 처리한다.
 * 로그인 전에는 첫 줄을 닉네임으로 처리하고,
 * 로그인 후에는 일반 채팅과 귓속말 명령을 분기한다.
 */
public class ChannelSession {

    private static final String WHISPER_PREFIX = "/w ";

    private final NioSession session;
    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ChatUseCase chatUseCase;

    private final ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();
    private String nickname;
    private boolean loggedIn = false;

    public ChannelSession(NioSession session, LoginUseCase loginUseCase,
                          LogoutUseCase logoutUseCase, ChatUseCase chatUseCase) {
        this.session = session;
        this.loginUseCase = loginUseCase;
        this.logoutUseCase = logoutUseCase;
        this.chatUseCase = chatUseCase;
    }

    // NonBlockingServer가 channel.read() 완료 후 호출
    public void processBuffer(ByteBuffer buffer) {
        while (buffer.hasRemaining()) {
            byte b = buffer.get();
            if (b == '\n') {
                processLine(lineBuffer.toString(StandardCharsets.UTF_8).trim());
                lineBuffer.reset();
            } else if (b != '\r') {
                lineBuffer.write(b);
            }
        }
    }

    public void close() {
        if (loggedIn) logoutUseCase.logout(nickname);
        session.close();
    }

    private void processLine(String line) {
        if (line.isBlank()) return;
        if (!loggedIn) {
            if (!loginUseCase.login(session, line)) {
                try { session.send("이미 사용 중인 닉네임입니다: " + line); } catch (IOException ignored) {}
                close();
                return;
            }
            nickname = line;
            loggedIn = true;
        } else if (line.startsWith(WHISPER_PREFIX)) {
            handleWhisper(line);
        } else {
            chatUseCase.sendAllClients(nickname, line);
        }
    }

    private void handleWhisper(String line) {
        String[] parts = line.substring(WHISPER_PREFIX.length()).split(" ", 2);
        if (parts.length < 2) {
            try { session.send("사용법: /w 닉네임 메시지"); } catch (IOException ignored) {}
            return;
        }
        chatUseCase.sendOneClient(nickname, parts[1], parts[0]);
    }
}