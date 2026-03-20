package dev.wonyoung.adapter.out;

import dev.wonyoung.application.port.out.AccountRepository;
import dev.wonyoung.domain.Account;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TextFileAccountRepository implements AccountRepository {

    private static final Path FILE_PATH = Path.of("fileset/output.csv");

    @Override
    public void save(Account account) {
        String line = account.accountNumber() + "," + account.name() + "," + account.balance() + System.lineSeparator();
        try {
            Files.writeString(FILE_PATH, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("계좌 저장 실패", e);
        }
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return readAll().stream()
                .filter(a -> a.accountNumber().equals(accountNumber))
                .findFirst();
    }

    @Override
    public boolean existsByAccountNumber(String accountNumber) {
        return findByAccountNumber(accountNumber).isPresent();
    }

    @Override
    public int count() {
        return readAll().size();
    }

    @Override
    public void update(Account account) {
        List<Account> updated = readAll().stream()
                .map(a -> a.accountNumber().equals(account.accountNumber()) ? account : a)
                .toList();
        writeAll(updated);
    }

    @Override
    public void deleteByAccountNumber(String accountNumber) {
        List<Account> remaining = readAll().stream()
                .filter(a -> !a.accountNumber().equals(accountNumber))
                .toList();
        writeAll(remaining);
    }

    private void writeAll(List<Account> accounts) {
        try {
            String content = accounts.stream()
                    .map(a -> a.accountNumber() + "," + a.name() + "," + a.balance())
                    .collect(Collectors.joining(System.lineSeparator()));
            Files.writeString(FILE_PATH,
                    content.isEmpty() ? "" : content + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("계좌 저장 실패", e);
        }
    }

    private List<Account> readAll() {
        if (!Files.exists(FILE_PATH)) return List.of();
        try {
            return Files.readAllLines(FILE_PATH).stream()
                    .filter(line -> !line.isBlank())
                    .map(line -> {
                        String[] parts = line.split(",");
                        return new Account(parts[0], parts[1], Long.parseLong(parts[2]));
                    })
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("계좌 읽기 실패", e);
        }
    }
}
