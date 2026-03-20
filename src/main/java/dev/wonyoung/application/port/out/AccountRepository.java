package dev.wonyoung.application.port.out;

import dev.wonyoung.domain.Account;

import java.util.Optional;

public interface AccountRepository {
    void save(Account account);
    Optional<Account> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);
    int count();
    void update(Account account);
    void deleteByAccountNumber(String accountNumber);
}
