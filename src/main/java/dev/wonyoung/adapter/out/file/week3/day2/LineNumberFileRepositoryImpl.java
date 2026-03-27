package dev.wonyoung.adapter.out.file.week3.day2;

import dev.wonyoung.application.exception.AppException;
import dev.wonyoung.application.port.out.week3.day2.LineNumberFileRepository;
import dev.wonyoung.infrastructure.container.di.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class LineNumberFileRepositoryImpl implements LineNumberFileRepository {

    @Override
    public List<String> readLines(String path) {
        List<String> lines = new ArrayList<>();
        try (LineNumberReader reader = new LineNumberReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (FileNotFoundException e) {
            throw new AppException("Source file not found: " + path, e);
        } catch (IOException e) {
            throw new AppException("Failed to read file: " + e.getMessage(), e);
        }
        return lines;
    }

    @Override
    public void writeLines(String path, List<String> lines) {
        try {
            Files.createDirectories(Path.of(path).getParent());
        } catch (IOException e) {
            throw new AppException("Failed to create directory: " + e.getMessage(), e);
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
            for (String line : lines) {
                writer.println(line);
            }
        } catch (IOException e) {
            throw new AppException("Failed to write file: " + e.getMessage(), e);
        }
    }
}
