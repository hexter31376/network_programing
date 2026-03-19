package dev.wonyoung.domain;

public class Account {

    private final String accountNumber;
    private final String name;
    private final long balance;

    public Account(String accountNumber, String name, long balance) {
        if (accountNumber == null || accountNumber.isBlank())
            throw new IllegalArgumentException("계좌번호는 필수입니다.");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("이름은 필수입니다.");
        if (balance < 0)
            throw new IllegalArgumentException("잔고는 0 이상이어야 합니다.");
        this.accountNumber = accountNumber;
        this.name = name;
        this.balance = balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getName() {
        return name;
    }

    public long getBalance() {
        return balance;
    }
}
