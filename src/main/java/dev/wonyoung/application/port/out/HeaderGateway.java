package dev.wonyoung.application.port.out;

import java.util.List;
import java.util.Map;

/**
 * HttpURLConnection을 통해 전체 헤더 정보를 가져오는 아웃바운드 포트.
 * <p>
 * HttpGateway와 달리 상태 라인, 모든 헤더, 응답 본문을 함께 반환한다.
 * Subject2(헤더 분석)와 Subject3(페이지 출력) 서비스가 이 포트를 사용한다.
 * </p>
 */
public interface HeaderGateway {

    void fetch(String url, ResponseHandler handler);

    interface ResponseHandler {
        /**
         * @param statusCode    HTTP 응답 코드 (예: 200)
         * @param statusMessage HTTP 응답 구문 (예: "OK")
         * @param headers       전체 헤더 맵 (null 키 = 상태 라인)
         * @param body          응답 본문 바이트 배열
         */
        void onResponse(int statusCode, String statusMessage,
                        Map<String, List<String>> headers, byte[] body);

        void onFailure(String errorMessage);
    }
}
