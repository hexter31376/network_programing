package dev.wonyoung;

import dev.wonyoung.common.config.FileServerAppConfig;

public class FileServerApplication {

    public static void main(String[] args) {
        FileServerAppConfig fileServerAppConfig = new FileServerAppConfig();
        fileServerAppConfig.startApp();
    }
}
