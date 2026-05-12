package dev.wonyoung.chatting;

import dev.wonyoung.chatting.adapter.in.swing.ChatClientController;
import dev.wonyoung.chatting.adapter.in.swing.ChatClientView;
import dev.wonyoung.chatting.application.service.ChatClientService;

import javax.swing.SwingUtilities;

public class ClientMain {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatClientView view = new ChatClientView();
            ChatClientService service = new ChatClientService();
            ChatClientController controller = new ChatClientController(view, service);
            controller.init();
        });
    }
}
