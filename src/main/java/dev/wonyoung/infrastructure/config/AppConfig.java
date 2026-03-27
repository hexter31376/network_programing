package dev.wonyoung.infrastructure.config;

import dev.wonyoung.infrastructure.container.Container;
import dev.wonyoung.infrastructure.container.aop.handler.ExceptionHandlingInterceptor;

public class AppConfig {

    public void startApp() {
        try {
            init();
        } catch (Exception e) {
            System.err.println("[FATAL] 애플리케이션 시작 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void init() throws Exception {
        Container container = new Container("dev.wonyoung");
        container.addInterceptor(new ExceptionHandlingInterceptor());
    }
}
