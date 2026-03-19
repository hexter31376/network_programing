package dev.wonyoung.application.port.service;

import dev.wonyoung.application.port.in.AccountUseCase;
import dev.wonyoung.application.port.out.AccountRepository;
import dev.wonyoung.domain.Account;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

@Component
public class AccountService implements AccountUseCase {

    private final AccountRepository accountRepository;

    @Inject
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void addAccount(String accountNumber, String name, long balance) {
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new IllegalArgumentException("이미 존재하는 계좌번호입니다: " + accountNumber);
        }
        accountRepository.save(new Account(accountNumber, name, balance));
    }

    @Override
    public Account findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계좌번호입니다: " + accountNumber));
    }

    @Override
    public int getCount() {
        return accountRepository.count();
    }
}
