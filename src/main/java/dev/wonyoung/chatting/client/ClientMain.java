package dev.wonyoung.chatting.client;

import dev.wonyoung.chatting.client.adapter.in.ChatSwingView;
import dev.wonyoung.chatting.client.application.service.ChatService;
import dev.wonyoung.common.container.Container;
import dev.wonyoung.common.container.aop.handler.ExceptionHandlingInterceptor;

import javax.swing.*;

public class ClientMain {

    public static void main(String[] args) throws Exception {
        Container container = new Container("dev.wonyoung.chatting.client");
        container.addInterceptor(new ExceptionHandlingInterceptor());
        ChatService chatService = container.get(ChatService.class);
        SwingUtilities.invokeLater(() -> new ChatSwingView(chatService));
    }
}
