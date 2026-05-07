package dev.wonyoung;

import dev.wonyoung.chatting.adapter.in.swing.ChatClientView;
import dev.wonyoung.chatting.adapter.in.swing.ChatServerView;
import dev.wonyoung.chatting.application.port.ChatClient;
import dev.wonyoung.chatting.application.port.ChatServer;

public class Application {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("사용법 : ./gradlew run --args=server");
            System.out.println("       ./gradlew run --args=client");
            return;
        }
        switch (args[0].toLowerCase()) {
            case "server" -> new ChatServer(new ChatServerView()).start();
            case "client" -> new ChatClient(new ChatClientView()).start();
            default -> System.out.println("알 수 없는 인수입니다 : " + args[0] + ". server 혹은 client 인수만 허용됩니다.");
        }
    }
}

