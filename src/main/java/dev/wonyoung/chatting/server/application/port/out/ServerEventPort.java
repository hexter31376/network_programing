package dev.wonyoung.chatting.server.application.port.out;

public interface ServerEventPort {
    void onServerStarted(int port);
    void onUserLoggedIn(String userId);
    void onUserLoggedOut(String userId);
}
