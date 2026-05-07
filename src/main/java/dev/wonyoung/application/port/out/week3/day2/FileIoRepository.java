package dev.wonyoung.application.port.out.week3.day2;

public interface FileIoRepository {
    void write(String path, String content);
    String read(String path);
}
