package dev.wonyoung.application.service;

import dev.wonyoung.application.port.in.FetchPageUseCase;
import dev.wonyoung.application.port.out.HeaderGateway;
import dev.wonyoung.domain.model.PageContent;
import dev.wonyoung.domain.model.enums.ContentKind;
import dev.wonyoung.domain.service.ContentClassifier;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * Subject3 서비스: 전체 헤더 + 콘텐츠를 가져오고, 10일 이내 수정 여부를 판단한다.
 */
@Component
public class FetchPageService implements FetchPageUseCase {

    private static final DateTimeFormatter HTTP_DATE =
            DateTimeFormatter.RFC_1123_DATE_TIME;

    private final HeaderGateway headerGateway;
    private final ContentClassifier classifier;

    @Inject
    public FetchPageService(HeaderGateway headerGateway, ContentClassifier classifier) {
        this.headerGateway = headerGateway;
        this.classifier    = classifier;
    }

    @Override
    public void fetch(String url, FetchCallback callback) {
        headerGateway.fetch(url, new HeaderGateway.ResponseHandler() {
            @Override
            public void onResponse(int statusCode, String statusMessage,
                                   Map<String, List<String>> headers, byte[] body) {
                String headersText    = formatHeaders(statusCode, statusMessage, headers);
                ContentKind kind      = resolveContentKind(headers);
                boolean withinTenDays = checkWithinTenDays(headers);
                callback.onResult(new PageContent(kind, headersText, body, withinTenDays));
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * 상태 라인 + 전체 헤더를 "Key: Value1, Value2" 형식의 문자열로 포매팅한다.
     */
    private String formatHeaders(int statusCode, String statusMessage,
                                 Map<String, List<String>> headers) {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP ").append(statusCode).append(' ').append(statusMessage).append('\n');
        headers.forEach((key, values) -> {
            if (key == null) return; // 상태 라인 항목은 이미 위에서 처리
            sb.append(key).append(": ")
              .append(String.join(", ", values))
              .append('\n');
        });
        return sb.toString();
    }

    /** Content-Type 헤더를 ContentClassifier로 분류한다. */
    private ContentKind resolveContentKind(Map<String, List<String>> headers) {
        List<String> values = headers.get("Content-Type");
        if (values == null || values.isEmpty()) return ContentKind.OTHER;
        return classifier.classify(values.get(0));
    }

    /**
     * Last-Modified 헤더를 파싱하여 현재 시각 기준 10일 이내인지 확인한다.
     * 헤더가 없으면 true로 간주한다 (서버가 날짜를 제공하지 않는 경우 허용).
     */
    private boolean checkWithinTenDays(Map<String, List<String>> headers) {
        List<String> values = headers.get("Last-Modified");
        if (values == null || values.isEmpty()) return true;

        try {
            ZonedDateTime lastModified = ZonedDateTime.parse(values.get(0), HTTP_DATE);
            return lastModified.isAfter(ZonedDateTime.now().minusDays(10));
        } catch (DateTimeParseException e) {
            return true; // 파싱 실패 시 허용
        }
    }
}
