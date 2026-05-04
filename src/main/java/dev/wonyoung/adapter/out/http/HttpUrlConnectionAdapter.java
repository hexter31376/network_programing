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

    /**
     * 실제 HTTP 요청을 수행하는 내부 메서드. 별도 스레드에서 호출된다.
     * <p>
     * 연결 → 상태 코드 확인 → 헤더 수집 → 본문 읽기 → 콜백 순서로 처리하며,
     * IOException 발생 시 onFailure 콜백을 호출한다.
     * finally 블록에서 반드시 연결을 끊어 소켓 리소스를 해제한다.
     * </p>
     */
    private void doFetch(String url, ResponseHandler handler) {
        HttpURLConnection conn = null;
        try {
            // URL을 파싱하여 커넥션 객체를 생성하고 타임아웃 등 공통 옵션을 설정한다.
            conn = openConnection(url);

            // 실제 TCP 연결을 수립한다. (connect()를 명시적으로 호출하지 않아도
            // getResponseCode()에서 암묵적으로 연결되지만, 명시적으로 분리해 오류 위치를 명확히 한다.)
            conn.connect();

            // HTTP 상태 코드(예: 200, 404)와 상태 문구(예: "OK", "Not Found")를 읽는다.
            int statusCode = conn.getResponseCode();
            String statusMessage = conn.getResponseMessage();

            // getHeaderFields()는 null 키에 상태 라인("HTTP/1.1 200 OK"),
            // 나머지 키에 헤더 이름을 담는다. 하나의 헤더에 값이 여러 개일 수 있어 List<String>으로 반환된다.
            Map<String, List<String>> headers = conn.getHeaderFields();

            // 상태 코드에 따라 정상 스트림 또는 에러 스트림으로 본문을 읽는다.
            byte[] body = readBody(conn, statusCode);

            // 수집한 결과를 콜백으로 상위 레이어에 전달한다.
            handler.onResponse(statusCode, statusMessage, headers, body);

        } catch (IOException e) {
            // 네트워크 오류, DNS 실패, 타임아웃 등 I/O 관련 예외를 모두 여기서 처리한다.
            handler.onFailure("요청 실패: " + e.getMessage());
        } finally {
            // 성공/실패 여부와 무관하게 커넥션을 반드시 종료해 소켓 리소스 누수를 방지한다.
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
