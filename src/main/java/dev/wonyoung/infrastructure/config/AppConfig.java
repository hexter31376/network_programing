package dev.wonyoung.infrastructure.config;

import dev.wonyoung.adapter.in.console.week5.Subject1Presenter;
import dev.wonyoung.adapter.in.console.week5.Subject2Presenter;
import dev.wonyoung.adapter.in.console.week5.Subject3Presenter;
import dev.wonyoung.infrastructure.container.Container;
import dev.wonyoung.infrastructure.container.aop.handler.ExceptionHandlingInterceptor;

import javax.swing.*;

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

        SwingUtilities.invokeLater(() -> {
            try {
                container.get(Subject1Presenter.class);
                container.get(Subject2Presenter.class);
                container.get(Subject3Presenter.class);
            } catch (Exception e) {
                System.err.println("[FATAL] UI 초기화 실패: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
