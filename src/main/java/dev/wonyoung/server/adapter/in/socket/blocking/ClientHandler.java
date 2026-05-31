package dev.wonyoung.server.adapter.in.socket.blocking;

import dev.wonyoung.server.application.port.in.ChatUseCase;
import dev.wonyoung.server.application.port.in.LoginUseCase;
import dev.wonyoung.server.application.port.in.LogoutUseCase;
import dev.wonyoung.server.application.port.out.ClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.UUID;

/**
 * 블로킹 서버에서 클라이언트 연결 하나를 담당하는 핸들러다.
 *
 * Runnable을 구현해 스레드 풀 위에서 실행되며,
 * BufferedReader.readLine으로 클라이언트 메시지를 한 줄씩 읽는다.
 * 첫 번째 줄은 닉네임으로 처리해 로그인을 시도하고,
 * 이후 줄은 일반 채팅 또는 /w 접두사로 시작하는 귓속말 명령으로 분기한다.
 * ClientSession도 구현해 ClientSessionStore에 직접 등록된다.
 */
public class ClientHandler implements ClientSession, Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private static final String WHISPER_PREFIX = "/w ";

    private final String id;
    private final Socket socket;
    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ChatUseCase chatUseCase;

    private BufferedWriter writer;
    private String nickname;

    public ClientHandler(Socket socket, LoginUseCase loginUseCase,
                         LogoutUseCase logoutUseCase, ChatUseCase chatUseCase) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.socket = socket;
        this.loginUseCase = loginUseCase;
        this.logoutUseCase = logoutUseCase;
        this.chatUseCase = chatUseCase;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void close() {
        try { socket.close(); } catch (IOException ignored) {}
    }

    @Override
    public void send(String message) throws IOException {
        writer.write(message);
        writer.newLine();
        writer.flush();
    }

    @Override
    public void run() {
        try (socket;
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {
            this.writer = bw;

            // 첫 줄 = 닉네임
            nickname = reader.readLine();
            if (nickname == null || nickname.isBlank()) return;

            if (!loginUseCase.login(this, nickname)) {
                send("이미 사용 중인 닉네임입니다: " + nickname);
                nickname = null; // finally에서 logout 호출 방지
                return;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(WHISPER_PREFIX)) {
                    handleWhisper(line);
                } else {
                    chatUseCase.sendAllClients(nickname, line);
                }
            }
        } catch (IOException e) {
            if (!socket.isClosed()) logger.error("클라이언트 {} 오류", id, e);
        } finally {
            if (nickname != null) logoutUseCase.logout(nickname);
        }
    }

    // "/w targetNickname message" 파싱
    private void handleWhisper(String line) {
        String[] parts = line.substring(WHISPER_PREFIX.length()).split(" ", 2);
        if (parts.length < 2) {
            try { send("사용법: /w 닉네임 메시지"); } catch (IOException ignored) {}
            return;
        }
        chatUseCase.sendOneClient(nickname, parts[1], parts[0]);
    }
}
