package dev.wonyoung.application.port.in.week3.day2;

import java.util.List;

public interface LineNumberUseCase {
    List<String> process(String sourcePath, String outputPath);
}
