package dev.wonyoung.application.service;

import dev.wonyoung.application.port.in.FetchHeadersUseCase;
import dev.wonyoung.application.port.out.HeaderGateway;
import dev.wonyoung.domain.model.HeaderResult;
import dev.wonyoung.domain.model.enums.ContentKind;
import dev.wonyoung.domain.service.ContentClassifier;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import java.util.List;
import java.util.Map;

/**
 * Subject2 서비스: 헤더를 읽어 ContentKind 분류 결과를 반환한다.
 */
@Component
public class FetchHeadersService implements FetchHeadersUseCase {

    private final HeaderGateway headerGateway;
    private final ContentClassifier classifier;

    @Inject
    public FetchHeadersService(HeaderGateway headerGateway, ContentClassifier classifier) {
        this.headerGateway = headerGateway;
        this.classifier = classifier;
    }

    @Override
    public void fetch(String url, FetchCallback callback) {
        headerGateway.fetch(url, new HeaderGateway.ResponseHandler() {
            @Override
            public void onResponse(int statusCode, String statusMessage,
                                   Map<String, List<String>> headers, byte[] body) {
                String statusLine = "HTTP " + statusCode + " " + statusMessage;
                ContentKind kind  = resolveContentKind(headers);
                callback.onResult(new HeaderResult(statusLine, headers, kind));
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    /** Content-Type 헤더 값을 꺼내 ContentClassifier로 분류한다. */
    private ContentKind resolveContentKind(Map<String, List<String>> headers) {
        List<String> values = headers.get("Content-Type");
        if (values == null || values.isEmpty()) return ContentKind.OTHER;
        return classifier.classify(values.get(0));
    }
}
