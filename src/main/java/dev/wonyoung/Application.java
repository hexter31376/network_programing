package dev.wonyoung;

import dev.wonyoung.adapter.in.console.week2.day1.subject.Subject1;
import dev.wonyoung.adapter.in.console.week2.day1.subject.Subject2;
import dev.wonyoung.infrastructure.container.Container;

public class Application {
    public static void main(String[] args) throws Exception {
        Container container = new Container();
        container.scan("dev.wonyoung");

        container.get(Subject1.class).start();
        container.get(Subject2.class).start();
    }
}
