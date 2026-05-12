package dev.wonyoung.chatting.domain.user.application.port.out;

public interface UserStorePort {
    void add(String name);
    void remove(String name);
    boolean contains(String name);
}
