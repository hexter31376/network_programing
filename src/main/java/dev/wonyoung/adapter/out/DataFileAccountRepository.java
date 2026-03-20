package dev.wonyoung.adapter.out;

import dev.wonyoung.application.port.out.AccountRepository;
import dev.wonyoung.domain.Account;
import dev.wonyoung.infrastructure.container.di.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class DataFileAccountRepository implements AccountRepository {

    private static final Path FILE_PATH = Path.of("fileset/output.dat");

    @Override
    public void save(Account account) {
        try (DataOutputStream dos = new DataOutputStream(
                new FileOutputStream(FILE_PATH.toFile(), true))) {
            dos.writeUTF(account.accountNumber());
            dos.writeUTF(account.name());
            dos.writeLong(account.balance());
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
        try (DataOutputStream dos = new DataOutputStream(
                new FileOutputStream(FILE_PATH.toFile(), false))) {
            for (Account a : accounts) {
                dos.writeUTF(a.accountNumber());
                dos.writeUTF(a.name());
                dos.writeLong(a.balance());
            }
        } catch (IOException e) {
            throw new RuntimeException("계좌 저장 실패", e);
        }
    }

    private List<Account> readAll() {
        if (!Files.exists(FILE_PATH)) {
            return List.of();
        }
        List<Account> accounts = new ArrayList<>();
        try (DataInputStream dis = new DataInputStream(
                new FileInputStream(FILE_PATH.toFile()))) {
            while (dis.available() > 0) {
                String accountNumber = dis.readUTF();
                String name = dis.readUTF();
                long balance = dis.readLong();
                accounts.add(new Account(accountNumber, name, balance));
            }
        } catch (IOException e) {
            throw new RuntimeException("계좌 읽기 실패", e);
        }
        return accounts;
    }
}
