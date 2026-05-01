package dev.wonyoung.infrastructure.adapter.subject2;

import dev.wonyoung.domin.model.FileRequest;
import dev.wonyoung.domin.model.FileResponse;
import dev.wonyoung.domin.port.in.ServeFileUseCase;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class FileService implements ServeFileUseCase {

    private static final String DEFAULT_INDEX = "index.txt";
    private static final DateTimeFormatter HTTP_DATE = DateTimeFormatter.RFC_1123_DATE_TIME;

    private final FileServerConfig config;

    @Inject
    public FileService(FileServerConfig config) {
        this.config = config;
    }

    @Override
    public FileResponse serve(FileRequest request) {
        try {
            return resolveFile(request.path());
        } catch (IOException e) {
            return buildResponse("500 Internal Server Error", e.getMessage().getBytes(StandardCharsets.UTF_8));
        }
    }

    private FileResponse resolveFile(String requestedPath) throws IOException {
        String decodedPath = URLDecoder.decode(requestedPath, StandardCharsets.UTF_8);
        Path filePath = config.rootDirectory()
            .resolve(decodedPath.substring(1))
            .normalize();

        if (!filePath.startsWith(config.rootDirectory()))
            return buildResponse("403 Forbidden", "접근이 거부되었습니다.");

        if (Files.isDirectory(filePath))
            filePath = filePath.resolve(DEFAULT_INDEX);

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath))
            return buildResponse("404 Not Found", requestedPath + " 파일을 찾을 수 없습니다.");

        if (!isTextFile(filePath.getFileName().toString()))
            return buildResponse("403 Forbidden", "텍스트 파일만 제공합니다.");

        return buildResponse("200 OK", Files.readAllBytes(filePath));
    }

    private boolean isTextFile(String filename) {
        return filename.endsWith(".txt")
            || filename.endsWith(".html")
            || filename.endsWith(".htm")
            || filename.endsWith(".csv")
            || filename.endsWith(".xml")
            || filename.endsWith(".json")
            || filename.endsWith(".md");
    }

    private FileResponse buildResponse(String status, String message) {
        return buildResponse(status, message.getBytes(StandardCharsets.UTF_8));
    }

    private FileResponse buildResponse(String status, byte[] body) {
        String header = "HTTP/1.0 %s\r\n".formatted(status)
            + "Server: TextFileServer/1.0\r\n"
            + "Date: %s\r\n".formatted(ZonedDateTime.now().format(HTTP_DATE))
            + "Content-Type: text/plain; charset=UTF-8\r\n"
            + "Content-Length: %d\r\n".formatted(body.length)
            + "Connection: close\r\n"
            + "\r\n";

        try {
            var out = new ByteArrayOutputStream(header.length() + body.length);
            out.write(header.getBytes(StandardCharsets.US_ASCII));
            out.write(body);
            return new FileResponse(status, out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
