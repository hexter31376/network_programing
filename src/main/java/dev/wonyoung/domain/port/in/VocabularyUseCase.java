package dev.wonyoung.domain.port.in;

public interface VocabularyUseCase {
    String add(String word, String meaning);
    String get(String word);
    String delete(String word);
    String list();
}
