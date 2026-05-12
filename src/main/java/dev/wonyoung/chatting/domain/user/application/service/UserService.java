package dev.wonyoung.chatting.domain.user.application.service;

import dev.wonyoung.chatting.domain.user.application.port.in.UserUseCase;
import dev.wonyoung.chatting.domain.user.application.port.out.UserStorePort;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import java.util.Set;

@Component
public class UserService implements UserUseCase {

    private static final Set<String> RESERVED = Set.of("서버");

    private final UserStorePort store;

    @Inject
    public UserService(UserStorePort store) {
        this.store = store;
    }

    @Override
    public RegisterResult register(String name) {
        if (name.isBlank()) return new RegisterResult.Failure("이름을 입력해주세요.");
        if (RESERVED.contains(name)) return new RegisterResult.Failure("사용할 수 없는 이름입니다.");
        if (store.contains(name)) return new RegisterResult.Failure("이미 사용 중인 이름입니다.");
        store.add(name);
        return new RegisterResult.Success();
    }

    @Override
    public void remove(String name) {
        store.remove(name);
    }
}
