package dev.wonyoung.server.application.port.out;

import java.io.IOException;

public interface ClientSession {
    String getId();
    void send(String message) throws IOException;
    void close();
}
