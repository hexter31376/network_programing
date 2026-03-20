package dev.wonyoung.adapter.in.console.week2.day2.subject;

import dev.wonyoung.adapter.in.FileInspectPresenter;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import javax.swing.*;

@Component
public class Subject1 {

    private final FileInspectPresenter presenter;

    @Inject
    public Subject1(FileInspectPresenter presenter) {
        this.presenter = presenter;
    }

    public void start() {
        SwingUtilities.invokeLater(presenter::start);
    }
}
