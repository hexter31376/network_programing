package dev.wonyoung.adapter.out.file;

import dev.wonyoung.domain.model.Word;
import dev.wonyoung.domain.port.out.VocabularyRepository;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileVocabularyRepository implements VocabularyRepository {

    private final Path filePath;

    public FileVocabularyRepository(String filePath) {
        this.filePath = Path.of(filePath);
    }

    @Override
    public void save(Word word) {
        List<Word> words = readAll();
        words.removeIf(w -> w.word().equals(word.word()));
        words.add(word);
        writeAll(words);
    }

    @Override
    public Optional<Word> findByWord(String word) {
        return readAll().stream()
                .filter(w -> w.word().equals(word))
                .findFirst();
    }

    @Override
    public boolean delete(String word) {
        List<Word> words = readAll();
        boolean removed = words.removeIf(w -> w.word().equals(word));
        if (!removed) return false;
        writeAll(words);
        return true;
    }

    @Override
    public List<Word> findAll() {
        return readAll();
    }

    private List<Word> readAll() {
        if (!Files.exists(filePath)) return new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            List<Word> words = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",", 2);
                if (parts.length < 2) continue;
                words.add(new Word(parts[0], parts[1]));
            }
            return words;
        } catch (IOException e) {
            throw new RuntimeException("단어장 파일 읽기 실패: " + e.getMessage(), e);
        }
    }

    private void writeAll(List<Word> words) {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Word w : words) {
                writer.write(w.word() + "," + w.meaning());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("단어장 파일 저장 실패: " + e.getMessage(), e);
        }
    }
}
