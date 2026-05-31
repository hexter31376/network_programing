package dev.wonyoung.server.adapter.in.socket.virtual;

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
 * Virtual Thread 서버에서 클라이언트 연결 하나를 담당하는 핸들러다.
 *
 * ClientHandler와 로직이 동일하며 Virtual Thread 위에서 실행된다는 점만 다르다.
 * readLine 블로킹이 발생해도 JVM이 Carrier Thread를 반환하므로
 * OS 스레드를 점유하지 않는다.
 */
public class VirtualClientHandler implements ClientSession, Runnable {

    private static final Logger logger = LoggerFactory.getLogger(VirtualClientHandler.class);
    private static final String WHISPER_PREFIX = "/w ";

    private final String id;
    private final Socket socket;
    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ChatUseCase chatUseCase;

    private BufferedWriter writer;
    private String nickname;

    public VirtualClientHandler(Socket socket, LoginUseCase loginUseCase,
                                LogoutUseCase logoutUseCase, ChatUseCase chatUseCase) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.socket = socket;
        this.loginUseCase = loginUseCase;
        this.logoutUseCase = logoutUseCase;
        this.chatUseCase = chatUseCase;
    }

    @Override
    public String getId() { return id; }

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

            nickname = reader.readLine();
            if (nickname == null || nickname.isBlank()) return;

            if (!loginUseCase.login(this, nickname)) {
                send("이미 사용 중인 닉네임입니다: " + nickname);
                nickname = null;
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

    private void handleWhisper(String line) {
        String[] parts = line.substring(WHISPER_PREFIX.length()).split(" ", 2);
        if (parts.length < 2) {
            try { send("사용법: /w 닉네임 메시지"); } catch (IOException ignored) {}
            return;
        }
        chatUseCase.sendOneClient(nickname, parts[1], parts[0]);
    }
}
