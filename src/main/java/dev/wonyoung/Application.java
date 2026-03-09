package dev.wonyoung;

import dev.wonyoung.adapter.in.console.week1.day1.inclass.DisplayCharactor;
import dev.wonyoung.adapter.in.console.week1.day1.inclass.ReadCharactersIncr;
import dev.wonyoung.adapter.in.console.week1.day1.inclass.StreamCopier;
import dev.wonyoung.adapter.in.console.week1.day1.inclass.WriteToFile;
import dev.wonyoung.infrastructure.container.Container;

public class Application {
    public static void main(String[] args) throws Exception {
        Container container = new Container();
        container.scan("dev.wonyoung");

        // DisplayCharactor displayCharactor = container.get(DisplayCharactor.class);
        // displayCharactor.start();

        // ReadCharactersIncr readCharactersIncr = container.get(ReadCharactersIncr.class);
        // readCharactersIncr.start();

        // StreamCopier streamCopier = container.get(StreamCopier.class);
        // streamCopier.start();

        // WriteToFile writeToFile = container.get(WriteToFile.class);
        // writeToFile.start();
    }
}
