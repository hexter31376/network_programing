package dev.wonyoung.infrastructure.config;

import dev.wonyoung.infrastructure.container.Container;
import dev.wonyoung.infrastructure.container.aop.handler.ExceptionHandlingInterceptor;
import dev.wonyoung.presentation.port.in.Subject1;
import dev.wonyoung.presentation.port.in.Subject2;
import dev.wonyoung.presentation.port.in.Subject3;

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

        Subject1 subject1 = container.get(Subject1.class);
        Subject2 subject2 = container.get(Subject2.class);
        Subject3 subject3 = container.get(Subject3.class);

        new Thread(subject1::start, "subject1").start();
        new Thread(subject2::start, "subject2").start();
        new Thread(subject3::start, "subject3").start();
    }
}
