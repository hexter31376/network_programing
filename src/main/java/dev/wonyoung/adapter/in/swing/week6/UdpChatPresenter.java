package dev.wonyoung.adapter.in.swing.week6;

import dev.wonyoung.domain.port.in.ChatUseCase;

public class UdpChatPresenter {

    private final ChatUseCase chatUseCase;
    private final UdpChatView view;

    public UdpChatPresenter(ChatUseCase chatUseCase, UdpChatView view) {
        this.chatUseCase = chatUseCase;
        this.view = view;
    }

    public void bind() {
        view.setOnSubmit(message -> {
            chatUseCase.sendMessage(message);
            view.appendMessage("[send] " + message + "\n");
        });

        chatUseCase.startReceiving(msg -> view.appendMessage(
                String.format("[receive] address: %s | port: %d | length: %d\ncontents: %s\n",
                        msg.address(), msg.port(), msg.dataLength(), msg.content())
        ));
    }
}
