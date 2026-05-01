package dev.wonyoung.infrastructure.adapter.subject1;

import dev.wonyoung.domin.model.Message;
import dev.wonyoung.domin.port.in.EchoUseCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {

    private static final Logger log = Logger.getLogger(ClientHandler.class.getName());

    private final Socket socket;
    private final EchoUseCase echoUseCase;

    ClientHandler(Socket socket, EchoUseCase echoUseCase) {
        this.socket = socket;
        this.echoUseCase = echoUseCase;
    }

    @Override
    public void run() {
        try (socket;
             var reader = new BufferedReader(
                 new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             var writer = new PrintWriter(
                 new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
        ) {
            log.info("[POOL] Connected: " + socket.getRemoteSocketAddress());
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("[POOL] Received: " + line);
                var response = echoUseCase.echo(new Message(line, socket.getRemoteSocketAddress().toString()));
                writer.println(response.content());
            }
        } catch (IOException e) {
            log.warning("[POOL] Error: " + e.getMessage());
        }
    }
}
