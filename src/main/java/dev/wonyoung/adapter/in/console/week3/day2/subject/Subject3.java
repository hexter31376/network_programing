package dev.wonyoung.adapter.in.console.week3.day2.subject;

import dev.wonyoung.adapter.in.presenter.NetworkPresenter;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

@Component
public class Subject3 {

    private final NetworkPresenter presenter;

    @Inject
    public Subject3(NetworkPresenter presenter) {
        this.presenter = presenter;
    }

    public void start() {
        presenter.start();
    }
}
