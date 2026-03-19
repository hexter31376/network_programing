package dev.wonyoung.application.port.in;

import dev.wonyoung.domain.Account;

public interface AccountUseCase {
    void addAccount(String accountNumber, String name, long balance);
    Account findByAccountNumber(String accountNumber);
    int getCount();
}
