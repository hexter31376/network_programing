package dev.wonyoung;

import dev.wonyoung.infrastructure.container.Container;

public class Application {
    public static void main(String[] args) throws Exception {
        Container container = new Container();
        container.scan("dev.wonyoung");

        // DisplayCharacter displayCharacter = container.get(DisplayCharactor.class);
        // displayCharacter.start();

        // ReadCharactersIncr readCharactersIncr = container.get(ReadCharactersIncr.class);
        // readCharactersIncr.start();

        // StreamCopier streamCopier = container.get(StreamCopier.class);
        // streamCopier.start();

        // WriteToFile writeToFile = container.get(WriteToFile.class);
        // writeToFile.start();
    }
}
