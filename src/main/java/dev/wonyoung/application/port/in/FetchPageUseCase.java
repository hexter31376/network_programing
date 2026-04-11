package dev.wonyoung.application.port.in;

import dev.wonyoung.domain.model.PageContent;

/**
 * Subject3: URL의 콘텐츠(텍스트 또는 이미지)를 전체 헤더와 함께 가져오는 인바운드 포트.
 * <p>
 * Last-Modified 헤더를 분석하여 10일 이내 수정 여부도 판단한다.
 * </p>
 */
public interface FetchPageUseCase {

    void fetch(String url, FetchCallback callback);

    interface FetchCallback {
        void onResult(PageContent content);
        void onError(String message);
    }
}
