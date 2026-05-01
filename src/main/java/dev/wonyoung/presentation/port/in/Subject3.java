package dev.wonyoung.presentation.port.in;

import dev.wonyoung.infrastructure.container.di.Component;

@Component
public class Subject3 {

    public void start() {
        System.out.println("Subject3: 시작");
        // 비즈니스 로직 수행
        System.out.println("Subject3: 종료");
    }
}
