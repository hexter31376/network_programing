package dev.wonyoung.chatting.server.adapter.in;

import com.google.gson.Gson;
import dev.wonyoung.chatting.server.application.port.in.LoginUseCase;
import dev.wonyoung.chatting.server.application.port.in.LogoutUseCase;
import dev.wonyoung.chatting.server.application.port.out.ServerEventPort;
import dev.wonyoung.chatting.share.domain.Protocol;
import dev.wonyoung.chatting.share.dto.ProtocolMessage;
import dev.wonyoung.common.container.di.Component;
import dev.wonyoung.common.container.di.Inject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Component
public class TcpLoginServer {

    private static final int TCP_PORT = 12345;

    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ServerEventPort eventPort;
    private final Gson gson = new Gson();

    @Inject
    public TcpLoginServer(LoginUseCase loginUseCase, LogoutUseCase logoutUseCase, ServerEventPort eventPort) {
        this.loginUseCase = loginUseCase;
        this.logoutUseCase = logoutUseCase;
        this.eventPort = eventPort;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            eventPort.onServerStarted(TCP_PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[서버] 클라이언트 연결: " + clientSocket.getInetAddress());
                new ClientHandler(clientSocket).start();
            }
        }
    }

    private class ClientHandler extends Thread {

        private final Socket socket;
        private String loggedInUser = null;

        ClientHandler(Socket socket) {
            this.socket = socket;
            setDaemon(true);
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter writer = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

                String line;
                while ((line = reader.readLine()) != null) {
                    ProtocolMessage msg = gson.fromJson(line, ProtocolMessage.class);
                    ProtocolMessage response = handle(msg);
                    if (response != null) {
                        writer.println(gson.toJson(response));
                    }
                }
            } catch (IOException e) {
                System.err.println("[서버] 클라이언트 연결 오류: " + e.getMessage());
            } finally {
                // 창 강제 종료 등으로 연결이 끊긴 경우 자동 로그아웃
                if (loggedInUser != null) {
                    logoutUseCase.logout(loggedInUser);
                    System.out.println("[서버] " + loggedInUser + " 강제 로그아웃 처리");
                }
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        private ProtocolMessage handle(ProtocolMessage msg) {
            if (msg.type == Protocol.LOGIN) {
                ProtocolMessage response = loginUseCase.login(msg.sender);
                if (response.type == Protocol.LOGIN_SUCCESS) {
                    loggedInUser = msg.sender;
                }
                return response;
            }
            if (msg.type == Protocol.LOGOUT) {
                logoutUseCase.logout(msg.sender);
                loggedInUser = null;
                return ProtocolMessage.logoutSuccess();
            }
            return null;
        }
    }
}
