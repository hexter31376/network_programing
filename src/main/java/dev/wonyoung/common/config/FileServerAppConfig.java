package dev.wonyoung.common.config;

import dev.wonyoung.common.container.Container;
import dev.wonyoung.common.exception.ExceptionHandler;

public class FileServerAppConfig {

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
        container.addInterceptor(new ExceptionHandler());
    }
}
