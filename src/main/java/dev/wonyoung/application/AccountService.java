package dev.wonyoung.application;

import dev.wonyoung.domain.model.Account;
import dev.wonyoung.domain.port.in.AccountUseCase;
import dev.wonyoung.domain.port.out.AccountRepository;

import java.util.List;

public class AccountService implements AccountUseCase {

    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public String add(String accountNumber, String name, long balance) {
        try {
            if (repository.findByAccountNumber(accountNumber).isPresent()) {
                return "ERROR:이미 존재하는 계좌번호입니다";
            }
            repository.save(new Account(accountNumber, name, balance));
            return "OK:등록완료";
        } catch (Exception e) {
            return "ERROR:" + e.getMessage();
        }
    }

    @Override
    public String get(String accountNumber) {
        return repository.findByAccountNumber(accountNumber)
                .map(a -> "OK:" + a.accountNumber() + ":" + a.name() + ":" + a.balance())
                .orElse("ERROR:존재하지 않는 계좌번호입니다");
    }

    @Override
    public String update(String accountNumber, String name, long balance) {
        try {
            if (!repository.update(new Account(accountNumber, name, balance))) {
                return "ERROR:존재하지 않는 계좌번호입니다";
            }
            return "OK:수정완료";
        } catch (Exception e) {
            return "ERROR:" + e.getMessage();
        }
    }

    @Override
    public String delete(String accountNumber) {
        if (!repository.deleteByAccountNumber(accountNumber)) {
            return "ERROR:존재하지 않는 계좌번호입니다";
        }
        return "OK:삭제완료";
    }

    @Override
    public String list() {
        List<Account> accounts = repository.findAll();
        if (accounts.isEmpty()) return "OK:등록된 계좌가 없습니다";
        StringBuilder sb = new StringBuilder("OK:");
        for (int i = 0; i < accounts.size(); i++) {
            Account a = accounts.get(i);
            if (i > 0) sb.append("|");
            sb.append(a.accountNumber()).append(":").append(a.name()).append(":").append(a.balance());
        }
        return sb.toString();
    }

    @Override
    public List<Account> findAll() {
        return repository.findAll();
    }
}
