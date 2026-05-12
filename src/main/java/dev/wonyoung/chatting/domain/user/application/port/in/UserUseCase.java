package dev.wonyoung.chatting.domain.user.application.port.in;

public interface UserUseCase {

    sealed interface RegisterResult {
        record Success() implements RegisterResult {}
        record Failure(String reason) implements RegisterResult {}
    }

    RegisterResult register(String name);
    void remove(String name);
}
