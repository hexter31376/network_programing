package dev.wonyoung.application.port.out;

/**
 * 비동기 HTTP GET 요청을 전송하는 아웃바운드 포트.
 * <p>
 * Application 레이어는 이 인터페이스만 알고, 실제 구현체(JavaHttpClientAdapter)는 모른다.
 * Subject1 서비스가 사용한다.
 * </p>
 */
public interface HttpGateway {

    /**
     * @param url     요청할 URL 문자열
     * @param handler 성공/실패 결과를 받을 콜백
     */
    void sendAsync(String url, ResponseHandler handler);

    interface ResponseHandler {
        /** 응답이 정상적으로 도착했을 때 Content-Type과 본문 바이트를 전달한다. */
        void onSuccess(String contentType, byte[] body);

        /** 네트워크 오류 또는 요청 생성 실패 시 에러 메시지를 전달한다. */
        void onFailure(String errorMessage);
    }
}
