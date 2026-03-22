package dev.wonyoung;


import dev.wonyoung.infrastructure.config.AppConfig;

public class Application {
    public static void main(String[] args) throws Exception {
        AppConfig appConfig = new AppConfig();
        appConfig.startApp();
    }
}

