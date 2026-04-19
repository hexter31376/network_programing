package dev.wonyoung.domain.port.in;

import dev.wonyoung.domain.model.Account;

import java.util.List;

public interface AccountUseCase {
    String add(String accountNumber, String name, long balance);
    String get(String accountNumber);
    String update(String accountNumber, String name, long balance);
    String delete(String accountNumber);
    String list();
    List<Account> findAll();
}
