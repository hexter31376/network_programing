package dev.wonyoung.adapter.in.view;

import java.awt.event.ActionListener;

public interface AccountView {

    // 입력 읽기
    String getRegName();
    String getRegAccountNumber();
    String getRegBalance();
    String getSearchAccountNumber();
    String getUpdateAccountNumber();
    String getUpdateName();
    String getUpdateBalance();
    String getDeleteAccountNumber();

    // 결과 표시
    void setCount(int count);
    void showAccountResult(String name, long balance);
    void showError(String message);
    void showEmptyAccount();
    void clearRegistrationForm();
    void clearSearchResult();
    void clearUpdateForm();
    void clearDeleteForm();

    // Presenter가 리스너를 등록
    void bindAddListener(ActionListener listener);
    void bindOutputListener(ActionListener listener);
    void bindUpdateListener(ActionListener listener);
    void bindDeleteListener(ActionListener listener);

    void display();
}