package dev.wonyoung.chatting2;

import dev.wonyoung.chatting2.adapter.in.swing.ChatClient2Controller;
import dev.wonyoung.chatting2.adapter.in.swing.ChatClient2View;
import dev.wonyoung.chatting2.application.service.ChatClient2Service;

import javax.swing.SwingUtilities;

public class Client2Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatClient2View view = new ChatClient2View();
            ChatClient2Service service = new ChatClient2Service();
            ChatClient2Controller controller = new ChatClient2Controller(view, service);
            controller.init();
        });
    }
}
