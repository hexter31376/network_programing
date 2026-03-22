package dev.wonyoung.infrastructure.config;

import dev.wonyoung.infrastructure.container.Container;

public class AppConfig {

    public void startApp() {
        try {
            init();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    private void init() throws Exception{
        Container container = new Container();
        container.scan("dev.wonyoung");
    }
}
