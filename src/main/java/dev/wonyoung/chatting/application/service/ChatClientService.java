package dev.wonyoung.chatting.application.service;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class ChatClientService {

    private static final String HOST = "localhost";
    private static final int PORT = 9999;

    private Socket socket;
    private BufferedWriter writer;
    private volatile boolean connected = false;

    private Consumer<String> onMessage;
    private Runnable onAccepted;
    private Consumer<String> onRejected;
    private Runnable onDisconnected;

    public void connect(String username) {
        try {
            socket = new Socket(HOST, PORT);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            connected = true;

            Thread t = new Thread(() -> readLoop(username), "client-read");
            t.setDaemon(true);
            t.start();
        } catch (IOException e) {
            notify(onMessage, "서버 연결 실패: " + e.getMessage());
        }
    }

    private void readLoop(String username) {
        try {
            writer.write("USERNAME:" + username);
            writer.newLine();
            writer.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("ACCEPTED:")) {
                    if (onAccepted != null) onAccepted.run();
                } else if (line.startsWith("REJECTED:")) {
                    connected = false;
                    notify(onRejected, line.substring("REJECTED:".length()));
                    break;
                } else {
                    notify(onMessage, line);
                }
            }
        } catch (IOException e) {
            // 연결 끊김
        } finally {
            connected = false;
            if (onDisconnected != null) onDisconnected.run();
            close();
        }
    }

    public void send(String text) {
        if (text.isBlank() || !connected) return;
        try {
            writer.write("MESSAGE:" + text);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            connected = false;
            notify(onMessage, "전송 실패: " + e.getMessage());
        }
    }

    public void disconnect() {
        connected = false;
        close();
    }

    public void setOnMessage(Consumer<String> callback) {
        this.onMessage = callback;
    }

    public void setOnAccepted(Runnable callback) {
        this.onAccepted = callback;
    }

    public void setOnRejected(Consumer<String> callback) {
        this.onRejected = callback;
    }

    public void setOnDisconnected(Runnable callback) {
        this.onDisconnected = callback;
    }

    private void notify(Consumer<String> callback, String value) {
        if (callback != null) callback.accept(value);
    }

    private void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }
}
