package dev.wonyoung;

import dev.wonyoung.infrastructure.container.Container;

public class Application {
    public static void main(String[] args) throws Exception {
        Container container = new Container();
        container.scan("dev.wonyoung");

        // 예시: ConsoleView 인스턴스 가져오기
        var consoleView = container.get(dev.wonyoung.adapter.in.console.ConsoleView.class);
        consoleView.display();
    }
}
