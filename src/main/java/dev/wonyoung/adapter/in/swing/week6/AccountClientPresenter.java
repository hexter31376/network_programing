package dev.wonyoung.adapter.in.swing.week6;

import dev.wonyoung.domain.port.in.ChatUseCase;

public class AccountClientPresenter {

    private final ChatUseCase chatUseCase;
    private final AccountClientView view;

    public AccountClientPresenter(ChatUseCase chatUseCase, AccountClientView view) {
        this.chatUseCase = chatUseCase;
        this.view = view;
    }

    public void bind() {
        view.onAdd(() -> {
            String account = view.getAddAccount();
            String name = view.getAddName();
            String balance = view.getAddBalance();
            if (account.isEmpty() || name.isEmpty() || balance.isEmpty()) {
                view.appendResult("[오류] 계좌번호, 이름, 잔고를 모두 입력하세요\n");
                return;
            }
            chatUseCase.sendMessage("ADD:" + account + ":" + name + ":" + balance);
        });

        view.onGet(() -> {
            String account = view.getGetAccount();
            if (account.isEmpty()) {
                view.appendResult("[오류] 조회할 계좌번호를 입력하세요\n");
                return;
            }
            chatUseCase.sendMessage("GET:" + account);
        });

        view.onList(() -> chatUseCase.sendMessage("LIST"));

        view.onUpdate(() -> {
            String account = view.getUpdateAccount();
            String name = view.getUpdateName();
            String balance = view.getUpdateBalance();
            if (account.isEmpty() || name.isEmpty() || balance.isEmpty()) {
                view.appendResult("[오류] 계좌번호, 이름, 잔고를 모두 입력하세요\n");
                return;
            }
            chatUseCase.sendMessage("UPDATE:" + account + ":" + name + ":" + balance);
        });

        view.onDelete(() -> {
            String account = view.getDeleteAccount();
            if (account.isEmpty()) {
                view.appendResult("[오류] 삭제할 계좌번호를 입력하세요\n");
                return;
            }
            chatUseCase.sendMessage("DELETE:" + account);
        });

        chatUseCase.startReceiving(msg -> {
            String response = msg.content();
            String display;
            if (response.startsWith("OK:")) {
                String body = response.substring(3);
                // LIST 응답: 계좌번호:이름:잔고|계좌번호:이름:잔고 형식
                display = "[성공] " + body.replace("|", "\n       ") + "\n";
            } else {
                display = "[오류] " + response.substring(response.indexOf(":") + 1) + "\n";
            }
            view.appendResult(display);
        });
    }
}
