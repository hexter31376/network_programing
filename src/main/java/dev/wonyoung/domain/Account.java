package dev.wonyoung.domain;

public record Account(String accountNumber, String name, long balance) {

    public Account {
        if (accountNumber == null || accountNumber.isBlank())
            throw new IllegalArgumentException("계좌번호는 필수입니다.");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("이름은 필수입니다.");
        if (balance < 0)
            throw new IllegalArgumentException("잔고는 0 이상이어야 합니다.");
    }
}