package dev.wonyoung.application.port.in;

import dev.wonyoung.domain.model.HttpResult;
import dev.wonyoung.domain.model.UrlMeta;

/**
 * Subject1: URL을 입력받아 메타 정보와 응답 본문을 가져오는 인바운드 포트.
 */
public interface FetchUrlUseCase {

    /**
     * @param rawUrl   요청할 URL 문자열
     * @param callback 메타/본문/에러 결과를 받을 콜백
     */
    void fetch(String rawUrl, FetchCallback callback);

    interface FetchCallback {
        /** URL 파싱 결과(프로토콜, 호스트 등)를 전달한다. */
        void onMeta(UrlMeta meta);

        /** HTTP 응답 본문과 콘텐츠 분류 결과를 전달한다. */
        void onBody(HttpResult result);

        /** URL 파싱 실패 또는 네트워크 오류 메시지를 전달한다. */
        void onError(String message);
    }
}
