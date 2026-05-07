package dev.wonyoung.domain.port.out;

import dev.wonyoung.domain.model.Word;

import java.util.List;
import java.util.Optional;

public interface VocabularyRepository {
    void save(Word word);
    Optional<Word> findByWord(String word);
    boolean delete(String word);
    List<Word> findAll();
}
