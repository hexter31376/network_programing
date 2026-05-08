package dev.wonyoung.chatting.application.port;

import dev.wonyoung.chatting.adapter.in.swing.ChatClientView;

import javax.swing.SwingUtilities;
import java.io.*;
import java.net.*;

public class ChatClient {

    private static final String HOST = "localhost";
    private static final int PORT = 9999;

    private final ChatClientView view;
    private Socket socket;
    private BufferedWriter writer;
    private volatile boolean connected = false;

    public ChatClient(ChatClientView view) {
        this.view = view;
    }

    public void start() {
        view.setOnSubmit(this::handleSend);
        view.setOnReconnect(this::reconnect);
        connect();
    }

    private void connect() {
        try {
            socket = new Socket(HOST, PORT);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            connected = true;
            String serverIp = socket.getInetAddress().getHostAddress();
            appendToView("Connected to server '" + serverIp + "'.");

            Thread readThread = new Thread(this::readLoop, "client-read");
            readThread.setDaemon(true);
            readThread.start();
        } catch (IOException e) {
            appendToView("Failed to connect to server: " + e.getMessage());
        }
    }

    private void reconnect() {
        if (connected) {
            appendToView("Already connected to the server.");
            return;
        }
        appendToView("Reconnecting...");
        connect();
    }

    private void readLoop() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                appendToView(line);
            }
        } catch (IOException e) {
            // 클라이언트가 갑자기 끊겼을 경우 예외가 발생할 수 있음
        } finally {
            connected = false;
            appendToView("Disconnected from server.");
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private void handleSend(String message) {
        if (message.isBlank()) return;
        if (!connected) {
            appendToView("No server connected.");
            return;
        }
        if (message.equalsIgnoreCase("quit")) {
            disconnect();
            return;
        }
        try {
            writer.write(message);
            writer.newLine();
            writer.flush();
            appendToView("[Me] " + message);
        } catch (IOException e) {
            appendToView("Failed to send message: " + e.getMessage());
            connected = false;
        }
    }

    private void disconnect() {
        connected = false;
        try { socket.close(); } catch (IOException ignored) {}
    }

    private void appendToView(String message) {
        SwingUtilities.invokeLater(() -> view.appendChat(message));
    }
}
