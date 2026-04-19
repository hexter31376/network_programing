package dev.wonyoung.adapter.in.swing.week6;

import dev.wonyoung.domain.port.in.ChatUseCase;

public class VocabularyClientPresenter {

    private final ChatUseCase chatUseCase;
    private final VocabularyClientView view;

    public VocabularyClientPresenter(ChatUseCase chatUseCase, VocabularyClientView view) {
        this.chatUseCase = chatUseCase;
        this.view = view;
    }

    public void bind() {
        view.onAdd(() -> {
            String word = view.getWord();
            String meaning = view.getMeaning();
            if (word.isEmpty() || meaning.isEmpty()) {
                view.appendResult("[오류] 단어와 뜻을 모두 입력하세요\n");
                return;
            }
            chatUseCase.sendMessage("ADD:" + word + ":" + meaning);
        });

        view.onGet(() -> {
            String word = view.getWord();
            if (word.isEmpty()) {
                view.appendResult("[오류] 조회할 단어를 입력하세요\n");
                return;
            }
            chatUseCase.sendMessage("GET:" + word);
        });

        view.onDelete(() -> {
            String word = view.getWord();
            if (word.isEmpty()) {
                view.appendResult("[오류] 삭제할 단어를 입력하세요\n");
                return;
            }
            chatUseCase.sendMessage("DELETE:" + word);
        });

        view.onList(() -> chatUseCase.sendMessage("LIST"));

        chatUseCase.startReceiving(msg -> {
            String response = msg.content();
            String display = response.startsWith("OK:")
                    ? response.substring(3).replace("|", "\n") + "\n"
                    : "[오류] " + response.substring(response.indexOf(":") + 1) + "\n";
            view.appendResult(display);
        });
    }
}

