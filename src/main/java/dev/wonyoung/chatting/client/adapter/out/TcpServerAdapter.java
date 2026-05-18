package dev.wonyoung.chatting.client.adapter.out;

import com.google.gson.Gson;
import dev.wonyoung.chatting.client.application.port.out.ServerPort;
import dev.wonyoung.chatting.share.dto.ProtocolMessage;
import dev.wonyoung.common.container.di.Component;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Component
public class TcpServerAdapter implements ServerPort {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;

    private final Gson gson = new Gson();
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    private void ensureConnected() throws IOException {
        if (socket == null || socket.isClosed()) {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            writer = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        }
    }

    @Override
    public ProtocolMessage sendLogin(String userId) throws Exception {
        ensureConnected();
        writer.println(gson.toJson(ProtocolMessage.login(userId)));
        return gson.fromJson(reader.readLine(), ProtocolMessage.class);
    }

    @Override
    public ProtocolMessage sendLogout(String userId) throws Exception {
        ensureConnected();
        writer.println(gson.toJson(ProtocolMessage.logout(userId)));
        String line = reader.readLine();
        return line != null ? gson.fromJson(line, ProtocolMessage.class) : ProtocolMessage.logoutSuccess();
    }

    @Override
    public void close() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }
}
