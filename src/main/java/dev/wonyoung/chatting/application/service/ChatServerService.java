package dev.wonyoung.chatting.application.service;

import dev.wonyoung.chatting.domain.message.application.port.in.MessageUseCase;
import dev.wonyoung.chatting.domain.message.application.port.out.MessageSenderPort;
import dev.wonyoung.chatting.domain.user.application.port.in.UserUseCase;
import dev.wonyoung.chatting.domain.user.application.port.in.UserUseCase.RegisterResult;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import javax.swing.SwingUtilities;
import java.io.*;
import java.net.*;
import java.util.function.Consumer;

@Component
public class ChatServerService {

    private static final int PORT = 9999;

    private final UserUseCase userService;
    private final MessageUseCase messageService;
    private Consumer<String> onLog;

    @Inject
    public ChatServerService(UserUseCase userService, MessageUseCase messageService) {
        this.userService = userService;
        this.messageService = messageService;
    }

    public void setOnLog(Consumer<String> callback) {
        this.onLog = callback;
    }

    public void start() {
        Thread t = new Thread(this::acceptLoop, "server-accept");
        t.setDaemon(true);
        t.start();
        log("서버 시작!");
        log("클라이언트 연결을 기다립니다.");
    }

    private void acceptLoop() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                try {
                    ClientHandler handler = new ClientHandler(clientSocket, this);
                    Thread t = new Thread(handler, "client-handler");
                    t.setDaemon(true);
                    t.start();
                } catch (IOException e) {
                    log("클라이언트 처리 실패: " + e.getMessage());
                    try { clientSocket.close(); } catch (IOException ignored) {}
                }
            }
        } catch (IOException e) {
            log("서버 오류: " + e.getMessage());
        }
    }

    void onJoined(ClientHandler handler, String username) {
        messageService.register(handler);
        log("[" + username + "] 접속");
        messageService.broadcast("[" + username + "] 님이 입장했습니다.", handler);
    }

    void onLeft(ClientHandler handler, String username) {
        messageService.unregister(handler);
        if (username != null) {
            userService.remove(username);
            messageService.broadcast("[" + username + "] 님이 퇴장했습니다.", null);
            log("[" + username + "] 퇴장");
        }
    }

    void log(String message) {
        if (onLog != null) SwingUtilities.invokeLater(() -> onLog.accept(message));
    }

    static class ClientHandler implements Runnable, MessageSenderPort {

        private final Socket socket;
        private final BufferedWriter writer;
        private final ChatServerService service;
        private String username;

        ClientHandler(Socket socket, ChatServerService service) throws IOException {
            this.socket = socket;
            this.service = service;
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (username == null) handleHandshake(line);
                    else handleMessage(line);
                }
            } catch (IOException e) {
                // 클라이언트 끊김
            } finally {
                service.onLeft(this, username);
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        private void handleHandshake(String line) {
            if (!line.startsWith("USERNAME:")) {
                send("REJECTED:잘못된 프로토콜");
                return;
            }
            String name = line.substring("USERNAME:".length()).trim();
            RegisterResult result = service.userService.register(name);
            if (result instanceof RegisterResult.Failure f) {
                send("REJECTED:" + f.reason());
                return;
            }
            username = name;
            send("ACCEPTED:" + name);
            service.onJoined(this, name);
        }

        private void handleMessage(String line) {
            if (!line.startsWith("MESSAGE:")) return;
            String text = line.substring("MESSAGE:".length());
            String formatted = "[" + username + "] " + text;
            service.log(formatted);
            service.messageService.broadcast(formatted, this);
        }

        @Override
        public void send(String message) {
            try {
                writer.write(message);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                // 클라이언트 끊김
            }
        }
    }
}
