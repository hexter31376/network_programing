package dev.wonyoung.server.application.port.in;

public interface LogoutUseCase {
    void logout(String nickname);
    void shutdownAll();
}
