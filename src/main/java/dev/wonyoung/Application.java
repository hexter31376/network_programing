package dev.wonyoung;

import dev.wonyoung.infrastructure.container.Container;

public class Application {
    public static void main(String[] args) throws Exception {
        Container container = new Container();
        container.scan("dev.wonyoung");
    }
}
