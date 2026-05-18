package dev.wonyoung.chatting.server.application.port.out;

public interface UserRepository {
    // ConcurrentHashMap Set.add()와 동일 의미: 신규 추가 true, 이미 존재 false (원자적)
    boolean add(String userId);
    void remove(String userId);
}
