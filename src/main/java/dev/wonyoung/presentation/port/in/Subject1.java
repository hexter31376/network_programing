package dev.wonyoung.presentation.port.in;

import dev.wonyoung.infrastructure.adapter.subject1.EchoServer;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

@Component
public class Subject1 {

    private final EchoServer echoServer;

    @Inject
    public Subject1(EchoServer echoServer) {
        this.echoServer = echoServer;
    }

    public void start() {
        echoServer.start();
    }
}
