package dev.wonyoung.application;

import dev.wonyoung.domain.model.Word;
import dev.wonyoung.domain.port.in.VocabularyUseCase;
import dev.wonyoung.domain.port.out.VocabularyRepository;

import java.util.List;

public class VocabularyService implements VocabularyUseCase {

    private final VocabularyRepository repository;

    public VocabularyService(VocabularyRepository repository) {
        this.repository = repository;
    }

    @Override
    public String add(String word, String meaning) {
        if (repository.findByWord(word).isPresent()) {
            return "ERROR:이미 존재하는 단어입니다 - " + word;
        }
        repository.save(new Word(word, meaning));
        return "OK:등록 완료 - " + word + " = " + meaning;
    }

    @Override
    public String get(String word) {
        return repository.findByWord(word)
                .map(w -> "OK:" + w.word() + " = " + w.meaning())
                .orElse("ERROR:단어를 찾을 수 없습니다 - " + word);
    }

    @Override
    public String delete(String word) {
        return repository.delete(word)
                ? "OK:삭제 완료 - " + word
                : "ERROR:단어를 찾을 수 없습니다 - " + word;
    }

    @Override
    public String list() {
        List<Word> words = repository.findAll();
        if (words.isEmpty()) {
            return "OK:단어장이 비어 있습니다";
        }
        String entries = words.stream()
                .map(w -> w.word() + "=" + w.meaning())
                .reduce((a, b) -> a + "|" + b)
                .orElse("");
        return "OK:" + entries;
    }
}
