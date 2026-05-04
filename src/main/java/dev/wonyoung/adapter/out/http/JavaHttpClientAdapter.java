package dev.wonyoung.adapter.out.http;

import dev.wonyoung.application.port.out.HttpGateway;
import dev.wonyoung.infrastructure.container.di.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Java 표준 라이브러리 HttpClient를 사용해 HttpGateway 포트를 구현한 어댑터.
 * <p>
 * HTTP 요청을 비동기로 전송하고, 결과(성공/실패)를 ResponseHandler 콜백으로 돌려준다.
 * Application 레이어는 이 클래스의 존재를 모르고 HttpGateway 인터페이스만 바라본다.
 * </p>
 */
@Component
public class JavaHttpClientAdapter implements HttpGateway {

    /**
     * 애플리케이션 전체에서 재사용하는 싱글턴 HttpClient.
     * - connectTimeout: TCP 연결 수립 제한 시간 (5초)
     * - followRedirects: 3xx 응답 시 자동으로 리다이렉트 추적 (NORMAL = http→http, https→https 허용)
     */
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /**
     * 주어진 URL로 비동기 GET 요청을 전송한다.
     * <p>
     * 요청 객체 생성에 실패하거나 네트워크 오류가 발생하면 handler.onFailure()를 호출한다.
     * 응답이 정상적으로 도착하면 handler.onSuccess()를 호출한다.
     * </p>
     *
     * @param url     요청을 보낼 대상 URL 문자열
     * @param handler 성공/실패 결과를 받을 콜백
     */
    @Override
    public void sendAsync(String url, ResponseHandler handler) {
        final HttpRequest request;
        try {
            // URI.create()로 문자열을 URI 객체로 변환하고 GET 요청 객체를 생성한다.
            // timeout: 서버로부터 응답을 기다리는 제한 시간 (10초)
            // 잘못된 URL 형식이면 여기서 예외가 발생한다.
            request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
        } catch (Exception e) {
            // URL 파싱 실패 등 요청 객체 자체를 만들지 못한 경우
            handler.onFailure("요청 생성 실패: " + e.getMessage());
            return;
        }

        // 비동기 요청 전송. 응답 본문을 byte[] 로 수신한다.
        // whenComplete()는 성공·실패 모두 호출되며, 별도 스레드에서 실행된다.
        HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
            .whenComplete((resp, error) -> {
                if (error != null) {
                    // 네트워크 타임아웃, DNS 실패 등 비동기 요청 자체가 실패한 경우
                    handler.onFailure("요청 실패: " + error.getMessage());
                    return;
                }

                // Content-Type 헤더가 없는 서버도 있으므로, 없으면 바이너리 기본값으로 대체한다.
                String contentType = resp.headers()
                        .firstValue("Content-Type")
                        .orElse("application/octet-stream");

                handler.onSuccess(contentType, resp.body());
            });
    }
}
