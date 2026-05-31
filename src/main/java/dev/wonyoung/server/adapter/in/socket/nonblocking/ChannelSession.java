package dev.wonyoung.server.adapter.in.socket.nonblocking;

import dev.wonyoung.server.application.port.in.ChatUseCase;
import dev.wonyoung.server.application.port.in.LoginUseCase;
import dev.wonyoung.server.application.port.in.LogoutUseCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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