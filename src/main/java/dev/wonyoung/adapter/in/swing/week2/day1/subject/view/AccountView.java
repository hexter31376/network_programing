package dev.wonyoung.adapter.in.swing.week2.day1.subject.view;

import java.awt.event.ActionListener;

public interface AccountView {

    // 입력 읽기
    String getRegName();
    String getRegAccountNumber();
    String getRegBalance();
    String getSearchAccountNumber();

    // 결과 표시
    void setCount(int count);
    void showAccountResult(String name, long balance);
    void showError(String message);
    void clearRegistrationForm();
    void clearSearchResult();

    // Presenter가 리스너를 등록
    void bindAddListener(ActionListener listener);
    void bindOutputListener(ActionListener listener);

    void display();
}
