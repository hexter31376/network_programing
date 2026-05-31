package dev.wonyoung.server.adapter.in.socket.asyncv2;

import dev.wonyoung.server.application.port.in.ChatUseCase;
import dev.wonyoung.server.application.port.in.LoginUseCase;
import dev.wonyoung.server.application.port.in.LogoutUseCase;
import dev.wonyoung.server.application.port.out.ClientSession;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * AsyncServerV2에서 클라이언트 연결 하나의 상태와 메시지 처리를 담당하는 세션 클래스다.
 *
 * processBuffer로 ByteBuffer를 받아 줄 단위로 파싱하고,
 * 로그인 전에는 첫 줄을 닉네임으로 처리하며 로그인 후에는 채팅과 귓속말을 분기한다.
 * send는 channel.write().get으로 전송 완료를 보장하고 synchronized로 동시 접근을 막는다.
 */
public class AsyncSessionV2 implements ClientSession {

    private static final String WHISPER_PREFIX = "/w ";

    private final String id;
    private final AsynchronousSocketChannel channel;
    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ChatUseCase chatUseCase;

    private final ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();
    private String nickname;
    private boolean loggedIn = false;

    public AsyncSessionV2(AsynchronousSocketChannel channel, LoginUseCase loginUseCase,
                          LogoutUseCase logoutUseCase, ChatUseCase chatUseCase) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.channel = channel;
        this.loginUseCase = loginUseCase;
        this.logoutUseCase = logoutUseCase;
        this.chatUseCase = chatUseCase;
    }

    @Override
    public String getId() { return id; }

    @Override
    public synchronized void send(String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap((message + "\n").getBytes(StandardCharsets.UTF_8));
        try {
            while (buffer.hasRemaining()) channel.write(buffer).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new IOException(e.getCause());
        }
    }

    // AsyncServerV2가 read 완료 후 호출 - 버퍼를 받아 줄 단위로 처리
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

    @Override
    public void close() {
        if (loggedIn) logoutUseCase.logout(nickname);
        try { channel.close(); } catch (IOException ignored) {}
    }

    private void processLine(String line) {
        if (line.isBlank()) return;
        if (!loggedIn) {
            if (!loginUseCase.login(this, line)) {
                try { send("이미 사용 중인 닉네임입니다: " + line); } catch (IOException ignored) {}
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
            try { send("사용법: /w 닉네임 메시지"); } catch (IOException ignored) {}
            return;
        }
        chatUseCase.sendOneClient(nickname, parts[1], parts[0]);
    }
}
