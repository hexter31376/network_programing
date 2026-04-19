package dev.wonyoung.domain.port.out;

import dev.wonyoung.domain.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    void save(Account account);
    Optional<Account> findByAccountNumber(String accountNumber);
    boolean update(Account account);
    boolean deleteByAccountNumber(String accountNumber);
    List<Account> findAll();
}
