package dev.wonyoung.adapter.in.swing.week2.day1.subject;

import dev.wonyoung.adapter.in.swing.week2.day1.subject.view.AccountView;
import dev.wonyoung.application.port.in.AccountUseCase;
import dev.wonyoung.domain.Account;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Component
public class AccountPresenter implements ActionListener {

    private final AccountView view;
    private final AccountUseCase accountUseCase;

    @Inject
    public AccountPresenter(AccountView view, AccountUseCase accountUseCase) {
        this.view = view;
        this.accountUseCase = accountUseCase;
        view.bindAddListener(this);
        view.bindOutputListener(this);
    }

    public void start() {
        view.display();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("추가".equals(e.getActionCommand())) {
            handleAdd();
        } else {
            handleOutput();
        }
    }

    private void handleAdd() {
        String name = view.getRegName();
        String accountNumber = view.getRegAccountNumber();
        String balanceStr = view.getRegBalance();

        if (name.isEmpty() || accountNumber.isEmpty() || balanceStr.isEmpty()) {
            view.showError("모든 필드를 입력하세요.");
            return;
        }

        long balance;
        try {
            balance = Long.parseLong(balanceStr);
        } catch (NumberFormatException ex) {
            view.showError("잔고는 숫자로 입력하세요.");
            return;
        }

        try {
            accountUseCase.addAccount(accountNumber, name, balance);
            view.setCount(accountUseCase.getCount());
            view.clearRegistrationForm();
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());
        }
    }

    private void handleOutput() {
        String accountNumber = view.getSearchAccountNumber();

        if (accountNumber.isEmpty()) {
            view.showError("계좌번호를 입력하세요.");
            return;
        }

        try {
            Account account = accountUseCase.findByAccountNumber(accountNumber);
            view.showAccountResult(account.getName(), account.getBalance());
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());
            view.clearSearchResult();
        }
    }
}