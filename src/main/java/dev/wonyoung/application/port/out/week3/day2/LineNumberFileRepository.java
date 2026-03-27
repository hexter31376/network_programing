package dev.wonyoung.application.port.out.week3.day2;

import java.util.List;

public interface LineNumberFileRepository {
    List<String> readLines(String path);
    void writeLines(String path, List<String> lines);
}
