package dev.wonyoung.adapter.in;

import dev.wonyoung.adapter.in.view.AccountView;
import dev.wonyoung.application.port.in.AccountUseCase;
import dev.wonyoung.domain.Account;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * AccountPresenter는 AccountView와 AccountUseCase를 연결하는 역할을 합니다.
 * 사용자의 입력을 받아 AccountUseCase에 전달하고, 결과를 다시 AccountView에 보여줍니다.
 * 이 클래스는 ActionListener를 구현하여 버튼 클릭 이벤트를 처리합니다.
 */
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
        view.bindUpdateListener(this);
        view.bindDeleteListener(this);
    }

    public void start() {
        view.display();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "추가" -> handleAdd();
            case "조회" -> handleOutput();
            case "수정" -> handleUpdate();
            case "삭제" -> handleDelete();
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
            view.clearSearchResult();
            view.showEmptyAccount();
        }
    }

    private void handleUpdate() {
        String accountNumber = view.getUpdateAccountNumber();
        String name = view.getUpdateName();
        String balanceStr = view.getUpdateBalance();

        if (accountNumber.isEmpty() || name.isEmpty() || balanceStr.isEmpty()) {
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
            accountUseCase.updateAccount(accountNumber, name, balance);
            view.setCount(accountUseCase.getCount());
            view.clearUpdateForm();
        } catch (IllegalArgumentException ex) {
            view.showEmptyAccount();
        }
    }

    private void handleDelete() {
        String accountNumber = view.getDeleteAccountNumber();

        if (accountNumber.isEmpty()) {
            view.showError("계좌번호를 입력하세요.");
            return;
        }

        try {
            accountUseCase.deleteAccount(accountNumber);
            view.setCount(accountUseCase.getCount());
            view.clearDeleteForm();
        } catch (IllegalArgumentException ex) {
            view.showEmptyAccount();
        }
    }
}