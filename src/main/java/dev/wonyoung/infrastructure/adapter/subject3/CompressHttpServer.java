package dev.wonyoung.infrastructure.adapter.subject3;

import dev.wonyoung.domin.model.CompressRequest;
import dev.wonyoung.domin.port.in.CompressUseCase;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Component
public class CompressHttpServer {

    private static final Logger log = Logger.getLogger(CompressHttpServer.class.getName());
    private static final int PORT = 9090;
    private static final int CORE_POOL = Runtime.getRuntime().availableProcessors() * 2;
    private static final int MAX_POOL = 50;
    private static final long KEEP_ALIVE = 60L;

    private static final String FORM_HTML = """
        <!DOCTYPE html>
        <html lang="ko">
        <head><meta charset="UTF-8"><title>파일 압축 서비스</title></head>
        <body>
          <h2>파일 압축 서비스</h2>
          <form method="post" action="/compress">
            <label>파일 경로:
              <input name="path" size="40" placeholder="예: content/hello.txt" />
            </label>
            <button type="submit">압축 다운로드</button>
          </form>
        </body>
        </html>
        """;

    private final CompressUseCase compressUseCase;

    @Inject
    public CompressHttpServer(CompressUseCase compressUseCase) {
        this.compressUseCase = compressUseCase;
    }

    public void start() {
        var executor = new ThreadPoolExecutor(
            CORE_POOL, MAX_POOL, KEEP_ALIVE, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(200),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );

        try (var serverSocket = new ServerSocket(PORT)) {
            log.info("Compress HTTP Server on http://localhost:" + PORT);
            while (!serverSocket.isClosed()) {
                var client = serverSocket.accept();
                executor.submit(() -> handle(client));
            }
        } catch (IOException e) {
            log.warning("Server error: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    private void handle(Socket socket) {
        try (socket;
             var in = new BufferedReader(
                 new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isBlank()) return;

            int contentLength = 0;
            String header;
            while ((header = in.readLine()) != null && !header.isBlank()) {
                if (header.toLowerCase().startsWith("content-length:"))
                    contentLength = Integer.parseInt(header.split(":")[1].trim());
            }

            String[] parts = requestLine.split(" ", 3);
            String method = parts[0];
            String path = parts[1];

            log.info("[%s] %s %s".formatted(socket.getRemoteSocketAddress(), method, path));

            if ("GET".equals(method) && "/".equals(path)) {
                out.write(buildHtmlResponse());
            } else if ("POST".equals(method) && "/compress".equals(path)) {
                char[] body = new char[contentLength];
                in.read(body, 0, contentLength);
                String filePath = parseFilePath(new String(body));
                out.write(buildCompressResponse(filePath));
            } else {
                out.write(buildTextResponse("404 Not Found", "페이지를 찾을 수 없습니다."));
            }
            out.flush();
        } catch (IOException e) {
            log.warning("Request error: " + e.getMessage());
        }
    }

    private String parseFilePath(String body) throws IOException {
        for (String param : body.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && "path".equals(kv[0]))
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
        }
        throw new IOException("경로가 전달되지 않았습니다.");
    }

    private byte[] buildCompressResponse(String filePath) throws IOException {
        Path source = Path.of(filePath);
        if (!Files.exists(source) || !Files.isRegularFile(source))
            return buildTextResponse("404 Not Found", filePath + " 파일을 찾을 수 없습니다.");

        byte[] content = Files.readAllBytes(source);
        String fileName = source.getFileName().toString();
        var result = compressUseCase.compress(new CompressRequest(fileName, content));

        log.info("Compressed: " + fileName + " → " + result.fileName());

        String header = "HTTP/1.0 200 OK\r\n"
            + "Content-Type: application/gzip\r\n"
            + "Content-Disposition: attachment; filename=\"" + result.fileName() + "\"\r\n"
            + "Content-Length: " + result.compressedContent().length + "\r\n"
            + "Connection: close\r\n\r\n";

        var baos = new ByteArrayOutputStream();
        baos.write(header.getBytes(StandardCharsets.US_ASCII));
        baos.write(result.compressedContent());
        return baos.toByteArray();
    }

    private byte[] buildHtmlResponse() throws IOException {
        byte[] body = FORM_HTML.getBytes(StandardCharsets.UTF_8);
        String header = "HTTP/1.0 200 OK\r\n"
            + "Content-Type: text/html; charset=UTF-8\r\n"
            + "Content-Length: " + body.length + "\r\n"
            + "Connection: close\r\n\r\n";
        var baos = new ByteArrayOutputStream();
        baos.write(header.getBytes(StandardCharsets.US_ASCII));
        baos.write(body);
        return baos.toByteArray();
    }

    private byte[] buildTextResponse(String status, String message) throws IOException {
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        String header = "HTTP/1.0 " + status + "\r\n"
            + "Content-Type: text/plain; charset=UTF-8\r\n"
            + "Content-Length: " + body.length + "\r\n"
            + "Connection: close\r\n\r\n";
        var baos = new ByteArrayOutputStream();
        baos.write(header.getBytes(StandardCharsets.US_ASCII));
        baos.write(body);
        return baos.toByteArray();
    }
}
