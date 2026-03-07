package dev.wonyoung.adapter.in.console;

import dev.wonyoung.infrastructure.container.di.Component;

@Component
public class ConsoleView {
    public void display() {
            System.out.println("콘솔 뷰입니다.");
    }
}
