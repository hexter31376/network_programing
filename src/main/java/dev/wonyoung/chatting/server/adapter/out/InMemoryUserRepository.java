package dev.wonyoung.chatting.server.adapter.out;

import dev.wonyoung.chatting.server.application.port.out.UserRepository;
import dev.wonyoung.common.container.di.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryUserRepository implements UserRepository {

    private final Set<String> users = ConcurrentHashMap.newKeySet();

    @Override
    public boolean add(String userId) {
        return users.add(userId);
    }

    @Override
    public void remove(String userId) {
        users.remove(userId);
    }
}
