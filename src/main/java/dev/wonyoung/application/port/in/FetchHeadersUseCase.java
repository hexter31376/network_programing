package dev.wonyoung.application.port.in;

import dev.wonyoung.domain.model.HeaderResult;

/**
 * Subject2: URL의 응답 헤더를 읽어 분류 결과를 돌려주는 인바운드 포트.
 */
public interface FetchHeadersUseCase {

    void fetch(String url, FetchCallback callback);

    interface FetchCallback {
        void onResult(HeaderResult result);
        void onError(String message);
    }
}
