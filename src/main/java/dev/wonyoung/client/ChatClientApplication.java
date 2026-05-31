package dev.wonyoung.client;

import dev.wonyoung.client.adapter.in.swing.ChatClientController;
import dev.wonyoung.client.adapter.in.swing.ChatClientView;
import dev.wonyoung.client.adapter.out.socket.SocketChatClient;

import javax.swing.SwingUtilities;

public class ChatClientApplication {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatClientView view = new ChatClientView();
            SocketChatClient client = new SocketChatClient();
            new ChatClientController(view, client);
        });
    }
}
