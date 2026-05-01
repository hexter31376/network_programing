package dev.wonyoung.infrastructure.adapter.subject2;

import dev.wonyoung.domin.model.FileRequest;
import dev.wonyoung.domin.port.in.ServeFileUseCase;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {

    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    private final Socket socket;
    private final ServeFileUseCase serveFileUseCase;

    RequestHandler(Socket socket, ServeFileUseCase serveFileUseCase) {
        this.socket = socket;
        this.serveFileUseCase = serveFileUseCase;
    }

    @Override
    public void run() {
        try (socket;
             var in = new BufferedReader(
                 new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            String path = parseRequestPath(in);
            log.info("[%s] GET %s".formatted(socket.getRemoteSocketAddress(), path));

            var response = serveFileUseCase.serve(new FileRequest(path));
            out.write(response.content());
            out.flush();
        } catch (IOException e) {
            log.warning("Request error: " + e.getMessage());
        }
    }

    private String parseRequestPath(BufferedReader in) throws IOException {
        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isBlank())
            throw new IOException("Empty request");

        while (true) {
            String line = in.readLine();
            if (line == null || line.isBlank()) break;
        }

        String[] parts = requestLine.split(" ", 3);
        if (parts.length < 2)
            throw new IOException("Malformed request: " + requestLine);

        String path = parts[1];
        int queryIdx = path.indexOf('?');
        return queryIdx >= 0 ? path.substring(0, queryIdx) : path;
    }
}
