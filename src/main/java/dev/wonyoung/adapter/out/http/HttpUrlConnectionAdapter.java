package dev.wonyoung.adapter.out.http;

import dev.wonyoung.application.port.out.HeaderGateway;
import dev.wonyoung.infrastructure.container.di.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * HttpURLConnection을 사용해 HeaderGateway 포트를 구현한 어댑터.
 * <p>
 * Java 표준 HttpURLConnection을 통해 상태 코드, 응답 구문, 전체 헤더, 응답 본문을 가져온다.
 * Subject2(헤더 분석)와 Subject3(페이지 출력) 서비스가 이 어댑터를 공유한다.
 * </p>
 */
@Component
public class HttpUrlConnectionAdapter implements HeaderGateway {

    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int READ_TIMEOUT_MS    = 10_000;

    @Override
    public void fetch(String url, ResponseHandler handler) {
        // 별도 스레드에서 블로킹 I/O를 수행하여 EDT(이벤트 디스패치 스레드)를 점유하지 않는다.
        Thread thread = new Thread(() -> doFetch(url, handler));
        thread.setDaemon(true);
        thread.start();
    }

    private void doFetch(String url, ResponseHandler handler) {
        HttpURLConnection conn = null;
        try {
            conn = openConnection(url);
            conn.connect();

            int statusCode = conn.getResponseCode();
            String statusMessage = conn.getResponseMessage();

            // getHeaderFields()는 null 키에 상태 라인, 나머지 키에 헤더 이름을 담는다.
            Map<String, List<String>> headers = conn.getHeaderFields();

            byte[] body = readBody(conn, statusCode);

            handler.onResponse(statusCode, statusMessage, headers, body);

        } catch (IOException e) {
            handler.onFailure("요청 실패: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /** URL 연결 객체를 생성하고 공통 옵션을 설정한다. */
    private HttpURLConnection openConnection(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        conn.setInstanceFollowRedirects(true);
        return conn;
    }

    /**
     * 응답 본문을 byte[]로 읽는다.
     * 4xx/5xx 오류 응답은 getErrorStream()을 통해 읽는다.
     */
    private byte[] readBody(HttpURLConnection conn, int statusCode) throws IOException {
        InputStream stream = statusCode >= 400
                ? conn.getErrorStream()
                : conn.getInputStream();

        if (stream == null) return new byte[0];

        try (stream) {
            return stream.readAllBytes();
        }
    }
}
