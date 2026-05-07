package dev.wonyoung.adapter.out.udp;

import dev.wonyoung.domain.model.Account;
import dev.wonyoung.domain.port.in.AccountUseCase;
import dev.wonyoung.domain.port.out.UdpSocketPort;

import java.util.List;
import java.util.function.Consumer;

public class AccountServerAdapter {

    private final UdpSocketPort socket;
    private final AccountUseCase useCase;
    private Consumer<String> logger = msg -> {};
    private Consumer<List<Account>> accountsRefresher = accounts -> {};

    public AccountServerAdapter(UdpSocketPort socket, AccountUseCase useCase) {
        this.socket = socket;
        this.useCase = useCase;
    }

    public void setLogger(Consumer<String> logger) {
        this.logger = logger;
    }

    public void setAccountsRefresher(Consumer<List<Account>> refresher) {
        this.accountsRefresher = refresher;
    }

    public void start() {
        Thread thread = new Thread(() -> {
            while (!socket.isClosed()) {
                try {
                    var received = socket.receive();
                    logger.accept("[요청] " + received.address() + ":" + received.port() + " -> " + received.content() + "\n");

                    String response = dispatch(received.content());
                    socket.send(response);
                    logger.accept("[응답] " + response + "\n");

                    accountsRefresher.accept(useCase.findAll());
                } catch (Exception e) {
                    if (!socket.isClosed()) {
                        logger.accept("[오류] " + e.getMessage() + "\n");
                    }
                }
            }
        }, "Account-Server");
        thread.setDaemon(true);
        thread.start();
    }

    private String dispatch(String raw) {
        String[] parts = raw.split(":", 4);
        String cmd = parts[0].toUpperCase();
        return switch (cmd) {
            case "ADD" -> {
                if (parts.length < 4) yield "ERROR:ADD 형식 오류 (ADD:계좌번호:이름:잔고)";
                try { yield useCase.add(parts[1], parts[2], Long.parseLong(parts[3])); }
                catch (NumberFormatException e) { yield "ERROR:잔고는 숫자여야 합니다"; }
            }
            case "GET" -> parts.length >= 2 ? useCase.get(parts[1]) : "ERROR:GET 형식 오류 (GET:계좌번호)";
            case "UPDATE" -> {
                if (parts.length < 4) yield "ERROR:UPDATE 형식 오류 (UPDATE:계좌번호:이름:잔고)";
                try { yield useCase.update(parts[1], parts[2], Long.parseLong(parts[3])); }
                catch (NumberFormatException e) { yield "ERROR:잔고는 숫자여야 합니다"; }
            }
            case "DELETE" -> parts.length >= 2 ? useCase.delete(parts[1]) : "ERROR:DELETE 형식 오류 (DELETE:계좌번호)";
            case "LIST" -> useCase.list();
            default -> "ERROR:알 수 없는 명령어 - " + cmd;
        };
    }
}
