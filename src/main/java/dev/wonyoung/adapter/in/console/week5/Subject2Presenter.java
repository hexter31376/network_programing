package dev.wonyoung.adapter.in.console.week5;

import dev.wonyoung.adapter.in.swing.week5.Subject2View;
import dev.wonyoung.application.port.in.FetchHeadersUseCase;
import dev.wonyoung.domain.model.HeaderResult;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.Map;

/**
 * Subject2 프레젠터: URL 제출 이벤트를 받아 헤더 조회를 요청하고 View에 결과를 전달한다.
 */
@Component
public class Subject2Presenter {

    private final FetchHeadersUseCase fetchHeaders;
    private final Subject2View subject2View;

    @Inject
    public Subject2Presenter(FetchHeadersUseCase fetchHeaders, Subject2View subject2View) {
        this.fetchHeaders = fetchHeaders;
        this.subject2View = subject2View;
        subject2View.setOnSubmit(this::onUrlSubmit);
    }

    private void onUrlSubmit(String rawUrl) {
        subject2View.showLoading();

        fetchHeaders.fetch(rawUrl, new FetchHeadersUseCase.FetchCallback() {
            @Override
            public void onResult(HeaderResult result) {
                SwingUtilities.invokeLater(() -> {
                    subject2View.showHeaders(formatHeaders(result));
                    subject2View.showClassified(formatKind(result));
                });
            }

            @Override
            public void onError(String message) {
                SwingUtilities.invokeLater(() -> subject2View.showHeaders(message));
            }
        });
    }

    /**
     * 헤더 맵을 "Key: Value1, Value2" 형식의 문자열로 변환한다.
     * 상태 라인(null 키)은 맨 위에 출력한다.
     */
    private static String formatHeaders(HeaderResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append(result.statusLine()).append('\n');

        for (Map.Entry<String, List<String>> entry : result.headers().entrySet()) {
            if (entry.getKey() == null) continue; // 상태 라인은 이미 출력
            sb.append(entry.getKey())
              .append(": ")
              .append(String.join(", ", entry.getValue()))
              .append('\n');
        }
        return sb.toString();
    }

    /** ContentKind를 한글 레이블로 변환한다. */
    private static String formatKind(HeaderResult result) {
        return switch (result.contentKind()) {
            case TEXT  -> "텍스트 파일";
            case IMAGE -> "이미지 파일";
            default -> "기타 파일";
        };
    }
}
