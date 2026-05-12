package dev.wonyoung.chatting.domain.user.infrastructure.out;

import dev.wonyoung.chatting.domain.user.application.port.out.UserStorePort;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class UserRepository implements UserStorePort {

    private final List<String> users;

    @Inject
    public UserRepository() {
        users = new CopyOnWriteArrayList<>();
    }

    @Override
    public void add(String name) { users.add(name); }

    @Override
    public void remove(String name) { users.remove(name); }

    @Override
    public boolean contains(String name) { return users.contains(name); }
}
