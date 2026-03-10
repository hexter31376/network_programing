package dev.wonyoung;

import dev.wonyoung.adapter.in.console.week1.day2.inclass.ReadCharString;
import dev.wonyoung.adapter.in.console.week1.day2.inclass.WriteNumberData;
import dev.wonyoung.infrastructure.container.Container;

public class Application {
    public static void main(String[] args) throws Exception {
        Container container = new Container();
        container.scan("dev.wonyoung");

        container.get(WriteNumberData.class).start();
        container.get(ReadCharString.class).start();
    }
}
