package dev.wonyoung.adapter.in.console.week2.day2.subject;

import dev.wonyoung.adapter.in.AccountPresenter;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import javax.swing.*;

@Component
public class Subject2 {

    private final AccountPresenter presenter;

    @Inject
    public Subject2(AccountPresenter presenter) {
        this.presenter = presenter;
    }

    public void start() {
        SwingUtilities.invokeLater(presenter::start);
    }
}
