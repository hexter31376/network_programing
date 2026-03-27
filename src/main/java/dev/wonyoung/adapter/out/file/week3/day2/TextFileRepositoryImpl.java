package dev.wonyoung.adapter.out.file.week3.day2;

import dev.wonyoung.application.exception.AppException;
import dev.wonyoung.application.port.out.week3.day2.FileIoRepository;
import dev.wonyoung.infrastructure.container.di.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class TextFileRepositoryImpl implements FileIoRepository {

    @Override
    public void write(String path, String content) {
        try {
            Files.createDirectories(Path.of(path).getParent());
        } catch (IOException e) {
            throw new AppException("Failed to create directory: " + e.getMessage(), e);
        }

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
            bw.write(content);
            bw.newLine();
        } catch (IOException e) {
            throw new AppException("Failed to write file: " + e.getMessage(), e);
        }
    }

    @Override
    public String read(String path) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new AppException("Failed to read file: " + e.getMessage(), e);
        }
        return sb.toString();
    }
}
