package dev.wonyoung.server;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import dev.wonyoung.server.adapter.in.socket.virtual.VirtualServer;
import dev.wonyoung.server.adapter.in.swing.ChatServerController;
import dev.wonyoung.server.adapter.in.swing.ChatServerView;
import dev.wonyoung.server.adapter.in.swing.TextAreaAppender;
import dev.wonyoung.server.application.port.in.ChatUseCase;
import dev.wonyoung.server.application.port.in.LoginUseCase;
import dev.wonyoung.server.application.port.in.LogoutUseCase;
import dev.wonyoung.server.adapter.in.socket.ChatServer;
import dev.wonyoung.common.container.Container;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;

public class VirtualChatServerApplication {

    public static void main(String[] args) throws Exception {
        Container container = new Container("dev.wonyoung.server");

        ChatServer server = new VirtualServer(
                container.get(LoginUseCase.class),
                container.get(LogoutUseCase.class),
                container.get(ChatUseCase.class)
        );
        container.register(ChatServer.class, server);

        SwingUtilities.invokeLater(() -> {
            ChatServerView view = new ChatServerView();
            wireLogAppender(view);
            new ChatServerController(view, server);
        });
    }

    private static void wireLogAppender(ChatServerView view) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        TextAreaAppender appender = new TextAreaAppender(view.getLogArea());
        appender.setContext(context);
        appender.start();
        context.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(appender);
    }
}
