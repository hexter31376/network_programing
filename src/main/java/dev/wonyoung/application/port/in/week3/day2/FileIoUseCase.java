package dev.wonyoung.application.port.in.week3.day2;

public interface FileIoUseCase {
    void save(String text);
    String load();
}
