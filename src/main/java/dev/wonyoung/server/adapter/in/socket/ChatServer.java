package dev.wonyoung.server.adapter.in.socket;

import java.io.IOException;

public interface ChatServer {
    void start() throws IOException;
    void stop();
}
