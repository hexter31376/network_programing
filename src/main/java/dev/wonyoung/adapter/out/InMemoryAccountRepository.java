package dev.wonyoung.adapter.out;

import dev.wonyoung.application.port.out.AccountRepository;
import dev.wonyoung.domain.Account;
import dev.wonyoung.infrastructure.container.di.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryAccountRepository implements AccountRepository {

    private final Map<String, Account> store = new LinkedHashMap<>();

    @Override
    public void save(Account account) {
        store.put(account.getAccountNumber(), account);
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return Optional.ofNullable(store.get(accountNumber));
    }

    @Override
    public boolean existsByAccountNumber(String accountNumber) {
        return store.containsKey(accountNumber);
    }

    @Override
    public int count() {
        return store.size();
    }
}
