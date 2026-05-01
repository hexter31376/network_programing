package dev.wonyoung.presentation.port.in;

import dev.wonyoung.infrastructure.adapter.subject3.CompressHttpServer;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

@Component
public class Subject3 {

    private final CompressHttpServer compressHttpServer;

    @Inject
    public Subject3(CompressHttpServer compressHttpServer) {
        this.compressHttpServer = compressHttpServer;
    }

    public void start() {
        compressHttpServer.start();
    }
}
