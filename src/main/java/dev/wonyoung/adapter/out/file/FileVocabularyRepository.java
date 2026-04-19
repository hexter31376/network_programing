package dev.wonyoung.adapter.out.file;

import dev.wonyoung.domain.model.Word;
import dev.wonyoung.domain.port.out.VocabularyRepository;

import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class FileVocabularyRepository implements VocabularyRepository {

    private final String filePath;
    private final Properties store = new Properties();

    public FileVocabularyRepository(String filePath) {
        this.filePath = filePath;
        load();
    }

    @Override
    public void save(Word word) {
        store.setProperty(word.word(), word.meaning());
        persist();
    }

    @Override
    public Optional<Word> findByWord(String word) {
        String meaning = store.getProperty(word);
        return meaning != null ? Optional.of(new Word(word, meaning)) : Optional.empty();
    }

    @Override
    public boolean delete(String word) {
        if (!store.containsKey(word)) return false;
        store.remove(word);
        persist();
        return true;
    }

    @Override
    public List<Word> findAll() {
        return store.stringPropertyNames().stream()
                .sorted()
                .map(w -> new Word(w, store.getProperty(w)))
                .toList();
    }

    private void load() {
        File file = new File(filePath);
        if (!file.exists()) return;
        try (InputStream in = new FileInputStream(file)) {
            store.load(in);
        } catch (IOException e) {
            throw new RuntimeException("단어장 파일 로드 실패: " + e.getMessage(), e);
        }
    }

    private void persist() {
        try (OutputStream out = new FileOutputStream(filePath)) {
            store.store(out, "Vocabulary");
        } catch (IOException e) {
            throw new RuntimeException("단어장 파일 저장 실패: " + e.getMessage(), e);
        }
    }
}