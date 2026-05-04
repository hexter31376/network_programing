package dev.wonyoung.application.service;

import dev.wonyoung.application.port.in.FetchUrlUseCase;
import dev.wonyoung.application.port.out.HttpGateway;
import dev.wonyoung.domain.model.HttpResult;
import dev.wonyoung.domain.model.UrlMeta;
import dev.wonyoung.domain.service.ContentClassifier;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import java.net.URI;

/**
 * Subject1 서비스: URL을 파싱해 메타 정보를 콜백으로 먼저 전달하고,
 * 이후 비동기 HTTP 요청으로 본문을 받아 ContentKind와 함께 콜백으로 전달한다.
 */
@Component
public class FetchUrlService implements FetchUrlUseCase {

    private final HttpGateway httpGateway;
    private final ContentClassifier classifier;

    @Inject
    public FetchUrlService(HttpGateway httpGateway, ContentClassifier classifier) {
        this.httpGateway = httpGateway;
        this.classifier  = classifier;
    }

    @Override
    public void fetch(String rawUrl, FetchCallback callback) {
        // URL 파싱: 실패 시 즉시 에러 콜백 후 종료
        final UrlMeta meta;
        try {
            meta = UrlMeta.from(URI.create(rawUrl).toURL());
        } catch (Exception e) {
            callback.onError("잘못된 URL: " + e.getMessage());
            return;
        }

        // URL 메타 정보를 먼저 콜백으로 전달 (HTTP 요청 전)
        callback.onMeta(meta);

        // 비동기 HTTP 요청 → 응답 도착 시 ContentKind 분류 후 본문 콜백 전달
        httpGateway.sendAsync(rawUrl, new HttpGateway.ResponseHandler() {
            @Override
            public void onSuccess(String contentType, byte[] body) {
                var kind = classifier.classify(contentType);
                var result = new HttpResult(kind, contentType, body);
                callback.onBody(result);
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }
}
