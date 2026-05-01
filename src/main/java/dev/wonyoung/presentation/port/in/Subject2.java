package dev.wonyoung.presentation.port.in;

import dev.wonyoung.infrastructure.adapter.subject2.FileServer;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

@Component
public class Subject2 {

    private final FileServer fileServer;

    @Inject
    public Subject2(FileServer fileServer) {
        this.fileServer = fileServer;
    }

    public void start() {
        fileServer.start();
    }
}
