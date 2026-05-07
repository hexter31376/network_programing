package dev.wonyoung.adapter.out.repository;

import dev.wonyoung.domain.model.Account;
import dev.wonyoung.domain.port.out.AccountRepository;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CsvAccountRepository implements AccountRepository {

    private final Path filePath;

    public CsvAccountRepository(String filePath) {
        this.filePath = Path.of(filePath);
    }

    @Override
    public void save(Account account) {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(toCsv(account));
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("계좌 저장 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return readAll().stream()
                .filter(a -> a.accountNumber().equals(accountNumber))
                .findFirst();
    }

    @Override
    public boolean update(Account account) {
        List<Account> accounts = readAll();
        boolean found = accounts.stream().anyMatch(a -> a.accountNumber().equals(account.accountNumber()));
        if (!found) return false;
        List<Account> updated = accounts.stream()
                .map(a -> a.accountNumber().equals(account.accountNumber()) ? account : a)
                .toList();
        writeAll(updated);
        return true;
    }

    @Override
    public boolean deleteByAccountNumber(String accountNumber) {
        List<Account> accounts = readAll();
        List<Account> filtered = accounts.stream()
                .filter(a -> !a.accountNumber().equals(accountNumber))
                .toList();
        if (filtered.size() == accounts.size()) return false;
        writeAll(filtered);
        return true;
    }

    @Override
    public List<Account> findAll() {
        return readAll();
    }

    private List<Account> readAll() {
        if (!Files.exists(filePath)) return new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            List<Account> accounts = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",", 3);
                if (parts.length < 3) continue;
                accounts.add(new Account(parts[0], parts[1], Long.parseLong(parts[2])));
            }
            return accounts;
        } catch (IOException e) {
            throw new RuntimeException("계좌 파일 읽기 실패: " + e.getMessage(), e);
        }
    }

    private void writeAll(List<Account> accounts) {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Account a : accounts) {
                writer.write(toCsv(a));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("계좌 파일 쓰기 실패: " + e.getMessage(), e);
        }
    }

    private String toCsv(Account a) {
        return a.accountNumber() + "," + a.name() + "," + a.balance();
    }
}
