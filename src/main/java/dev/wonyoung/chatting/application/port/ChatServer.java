package dev.wonyoung.chatting.application.port;

import dev.wonyoung.chatting.adapter.in.swing.ChatServerView;

import javax.swing.SwingUtilities;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatServer {

    private static final int PORT = 9999;

    private final ChatServerView view;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final AtomicInteger clientCounter = new AtomicInteger(0);

    public ChatServer(ChatServerView view) {
        this.view = view;
    }

    public void start() {
        view.setOnSubmit(this::handleServerMessage);

        Thread acceptThread = new Thread(this::acceptLoop, "server-accept");
        acceptThread.setDaemon(true);
        acceptThread.start();

        appendToView("Server started. Listening on port " + PORT + "...");
    }

    private void acceptLoop() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                try {
                    int clientId = clientCounter.incrementAndGet();
                    ClientHandler handler = new ClientHandler(clientSocket, this, clientId);
                    clients.add(handler);
                    Thread t = new Thread(handler, "client-#" + clientId);
                    t.setDaemon(true);
                    t.start();
                    handler.send("You are connected as Client #" + clientId);
                    appendToView("Client #" + clientId + " connected: " + clientSocket.getInetAddress().getHostAddress()
                            + " (Active: " + clients.size() + ")");
                } catch (IOException e) {
                    appendToView("Failed to handle incoming client: " + e.getMessage());
                    try { clientSocket.close(); } catch (IOException ignored) {}
                }
            }
        } catch (IOException e) {
            appendToView("Server error: " + e.getMessage());
        }
    }

    private void handleServerMessage(String message) {
        if (message.isBlank()) return;
        if (clients.isEmpty()) {
            appendToView("No client connected.");
            return;
        }
        String formatted = "[Server] " + message;
        appendToView(formatted);
        broadcast(formatted, null);
    }

    void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.send(message);
            }
        }
    }

    void removeClient(ClientHandler handler) {
        clients.remove(handler);
        appendToView("Client #" + handler.getClientId() + " disconnected: " + handler.getClientIp()
                + " (Active: " + clients.size() + ")");
    }

    void appendToView(String message) {
        SwingUtilities.invokeLater(() -> view.appendChat(message));
    }

    static class ClientHandler implements Runnable {

        private final Socket socket;
        private final BufferedWriter writer;
        private final ChatServer server;
        private final String clientIp;
        private final int clientId;

        ClientHandler(Socket socket, ChatServer server, int clientId) throws IOException {
            this.socket = socket;
            this.server = server;
            this.clientId = clientId;
            this.clientIp = socket.getInetAddress().getHostAddress();
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String formatted = "[#" + clientId + " | " + clientIp + "] " + line;
                    server.appendToView(formatted);
                    server.broadcast(formatted, this);
                }
            } catch (IOException e) {
                // 클라이언트가 갑자기 끊겼을 경우 예외가 발생할 수 있음
            } finally {
                server.removeClient(this);
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        void send(String message) {
            try {
                writer.write(message);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                // client disconnected
            }
        }

        String getClientIp() {
            return clientIp;
        }

        int getClientId() {
            return clientId;
        }
    }
}
