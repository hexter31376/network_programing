package dev.wonyoung.chatting2.application.service;

import dev.wonyoung.infrastructure.container.di.Component;

import javax.swing.SwingUtilities;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

@Component
public class ChatServer2Service {

    private static final int PORT = 9998;
    private static final Set<String> RESERVED = Set.of("서버");

    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final Set<String> usernames = new CopyOnWriteArraySet<>();
    private Consumer<String> onLog;

    public void setOnLog(Consumer<String> callback) {
        this.onLog = callback;
    }

    public void start() {
        Thread t = new Thread(this::acceptLoop, "server2-accept");
        t.setDaemon(true);
        t.start();
        log("서버 시작!");
        log("클라이언트 연결을 기다립니다.");
    }

    private void acceptLoop() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                try {
                    ClientHandler handler = new ClientHandler(socket, this);
                    Thread t = new Thread(handler, "client2-handler");
                    t.setDaemon(true);
                    t.start();
                } catch (IOException e) {
                    log("클라이언트 처리 실패: " + e.getMessage());
                    try { socket.close(); } catch (IOException ignored) {}
                }
            }
        } catch (IOException e) {
            log("서버 오류: " + e.getMessage());
        }
    }

    void broadcast(String message, ClientHandler exclude) {
        for (ClientHandler c : clients) {
            if (c != exclude) c.send(message);
        }
    }

    boolean registerUser(String name) {
        if (name.isBlank() || RESERVED.contains(name)) return false;
        return usernames.add(name);
    }

    void onJoined(ClientHandler handler, String username) {
        clients.add(handler);
        log("[" + username + "] 로그온");
        broadcast("[" + username + "] 님이 로그온했습니다.", handler);
    }

    void onLogout(ClientHandler handler, String username) {
        clients.remove(handler);
        if (username != null) {
            usernames.remove(username);
            broadcast("[" + username + "] 님이 로그아웃했습니다.", null);
            log("[" + username + "] 로그아웃");
        }
    }

    void log(String message) {
        if (onLog != null) SwingUtilities.invokeLater(() -> onLog.accept(message));
    }

    static class ClientHandler implements Runnable {

        private final Socket socket;
        private final BufferedWriter writer;
        private final ChatServer2Service service;
        private String username;

        ClientHandler(Socket socket, ChatServer2Service service) throws IOException {
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
            } catch (IOException ignored) {
            } finally {
                service.onLogout(this, username);
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        private void handleHandshake(String line) {
            if (!line.startsWith("USERNAME:")) {
                send("REJECTED:잘못된 프로토콜");
                return;
            }
            String name = line.substring("USERNAME:".length()).trim();
            if (name.isBlank()) {
                send("REJECTED:이름을 입력해주세요.");
                return;
            }
            if (!service.registerUser(name)) {
                send("REJECTED:사용할 수 없거나 이미 사용 중인 이름입니다.");
                return;
            }
            username = name;
            send("ACCEPTED:" + name);
            service.onJoined(this, name);
        }

        private void handleMessage(String line) {
            if (line.equals("LOGOUT")) {
                String name = username;
                username = null;
                service.onLogout(this, name);
                try { socket.close(); } catch (IOException ignored) {}
                return;
            }
            if (!line.startsWith("MESSAGE:")) return;
            String text = line.substring("MESSAGE:".length());
            String formatted = "[" + username + "] " + text;
            service.log(formatted);
            service.broadcast(formatted, this);
        }

        void send(String message) {
            try {
                writer.write(message);
                writer.newLine();
                writer.flush();
            } catch (IOException ignored) {}
        }
    }
}
