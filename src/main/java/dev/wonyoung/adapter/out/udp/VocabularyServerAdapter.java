package dev.wonyoung.adapter.out.udp;

import dev.wonyoung.domain.port.in.VocabularyUseCase;
import dev.wonyoung.domain.port.out.UdpSocketPort;

import java.util.function.Consumer;

public class VocabularyServerAdapter {

    private final UdpSocketPort socket;
    private final VocabularyUseCase useCase;
    private Consumer<String> logger = msg -> {};

    public VocabularyServerAdapter(UdpSocketPort socket, VocabularyUseCase useCase) {
        this.socket = socket;
        this.useCase = useCase;
    }

    public void setLogger(Consumer<String> logger) {
        this.logger = logger;
    }

    public void start() {
        Thread thread = new Thread(() -> {
            while (!socket.isClosed()) {
                try {
                    var received = socket.receive();
                    logger.accept("[요청] " + received.address() + ":" + received.port() + " → " + received.content() + "\n");

                    String response = dispatch(received.content());
                    socket.send(response);
                    logger.accept("[응답] " + response + "\n");
                } catch (Exception e) {
                    if (!socket.isClosed()) {
                        logger.accept("[오류] " + e.getMessage() + "\n");
                    }
                }
            }
        }, "Vocabulary-Server");
        thread.setDaemon(true);
        thread.start();
    }

    private String dispatch(String raw) {
        String[] parts = raw.split(":", 3);
        String cmd = parts[0].toUpperCase();
        return switch (cmd) {
            case "ADD"    -> parts.length >= 3 ? useCase.add(parts[1], parts[2]) : "ERROR:ADD 형식 오류 (ADD:단어:뜻)";
            case "GET"    -> parts.length >= 2 ? useCase.get(parts[1])            : "ERROR:GET 형식 오류 (GET:단어)";
            case "DELETE" -> parts.length >= 2 ? useCase.delete(parts[1])         : "ERROR:DELETE 형식 오류 (DELETE:단어)";
            case "LIST"   -> useCase.list();
            default       -> "ERROR:알 수 없는 명령어 - " + cmd;
        };
    }
}
