package dev.wonyoung.infrastructure.adapter.subject2;

import dev.wonyoung.infrastructure.container.di.Component;

import java.nio.file.Path;

@Component
public class FileServerConfig {

    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_ROOT = "content";

    private final Path rootDirectory;
    private final int port;

    public FileServerConfig() {
        this.rootDirectory = Path.of(DEFAULT_ROOT).toAbsolutePath().normalize();
        this.port = DEFAULT_PORT;
    }

    public Path rootDirectory() {
        return rootDirectory;
    }

    public int port() {
        return port;
    }
}
