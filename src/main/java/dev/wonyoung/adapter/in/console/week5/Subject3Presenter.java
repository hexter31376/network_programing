package dev.wonyoung.adapter.in.console.week5;

import dev.wonyoung.adapter.in.swing.week5.Subject3View;
import dev.wonyoung.application.port.in.FetchPageUseCase;
import dev.wonyoung.domain.model.PageContent;
import dev.wonyoung.domain.model.enums.ContentKind;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import javax.swing.*;
import java.awt.Image;
import java.nio.charset.StandardCharsets;

/**
 * Subject3 프레젠터: URL 제출 이벤트를 받아 페이지 조회를 요청하고 View에 결과를 전달한다.
 * <p>
 * - 10일 이내 수정된 텍스트 → JTextArea 출력
 * - 10일 이내 수정된 이미지 → JLabel 출력
 * - 10일 이상 지난 페이지  → 안내 메시지 출력
 * </p>
 */
@Component
public class Subject3Presenter {

    private static final String NOT_MODIFIED_MSG = "요청된 웹페이지가 10일 이내에 수정되지 않음";

    private final FetchPageUseCase fetchPage;
    private final Subject3View view;

    @Inject
    public Subject3Presenter(FetchPageUseCase fetchPage, Subject3View view) {
        this.fetchPage = fetchPage;
        this.view      = view;
        view.setOnSubmit(this::onUrlSubmit);
    }

    private void onUrlSubmit(String rawUrl) {
        view.showLoading();

        fetchPage.fetch(rawUrl, new FetchPageUseCase.FetchCallback() {
            @Override
            public void onResult(PageContent content) {
                SwingUtilities.invokeLater(() -> render(content));
            }

            @Override
            public void onError(String message) {
                SwingUtilities.invokeLater(() -> view.showMessage(message));
            }
        });
    }

    private void render(PageContent content) {
        view.showHeaders(content.headersText());

        if (!content.withinTenDays()) {
            view.showMessage(NOT_MODIFIED_MSG);
            return;
        }

        if (content.kind() == ContentKind.TEXT) {
            view.showText(new String(content.body(), StandardCharsets.UTF_8));
        } else if (content.kind() == ContentKind.IMAGE) {
            ImageIcon raw  = new ImageIcon(content.body());
            Image scaled = raw.getImage().getScaledInstance(400, -1, Image.SCALE_SMOOTH);
            view.showImage(new ImageIcon(scaled));
        } else {
            view.showMessage("[지원되지 않는 콘텐츠 유형]");
        }
    }
}
