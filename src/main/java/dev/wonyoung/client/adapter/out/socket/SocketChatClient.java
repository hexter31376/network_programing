package dev.wonyoung.client.adapter.out.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class SocketChatClient {

    private static final Logger logger = LoggerFactory.getLogger(SocketChatClient.class);

    private Socket socket;
    private BufferedWriter writer;
    private Thread receiveThread;

    public void connect(String host, int port, String nickname, Consumer<String> onMessage, Runnable onDisconnect) throws IOException {
        socket = new Socket(host, port);
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        // 첫 줄 = 닉네임
        writer.write(nickname);
        writer.newLine();
        writer.flush();

        receiveThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    onMessage.accept(line);
                }
            } catch (IOException e) {
                if (!socket.isClosed()) {
                    logger.error("수신 오류", e);
                }
            } finally {
                onDisconnect.run();
            }
        });
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    public void send(String message) throws IOException {
        writer.write(message);
        writer.newLine();
        writer.flush();
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            logger.error("소켓 종료 오류", e);
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}
