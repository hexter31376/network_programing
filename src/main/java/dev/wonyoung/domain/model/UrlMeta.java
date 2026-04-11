package dev.wonyoung.domain.model;

import java.net.URL;

/**
 * java.net.URL에서 추출한 URL 구성 요소를 담는 도메인 모델.
 *
 * @param protocol    프로토콜 (예: "https")
 * @param host        호스트명 (예: "example.com")
 * @param port        명시된 포트 번호, 없으면 -1
 * @param defaultPort 프로토콜 기본 포트 (http=80, https=443)
 * @param file        경로 + 쿼리 문자열
 * @param path        경로만 (쿼리 제외)
 * @param query       쿼리 문자열 (?이후), 없으면 null
 * @param ref         앵커(#이후), 없으면 null
 * @param userInfo    사용자 정보 (user:password), 없으면 null
 * @param authority   host[:port] 형태의 권한 정보
 * @param urlHashCode URL 객체의 hashCode
 */
public record UrlMeta(
        String protocol,
        String host,
        int    port,
        int    defaultPort,
        String file,
        String path,
        String query,
        String ref,
        String userInfo,
        String authority,
        int    urlHashCode
) {
    /** java.net.URL 객체에서 모든 필드를 추출해 UrlMeta를 생성한다. */
    public static UrlMeta from(URL url) {
        return new UrlMeta(
                url.getProtocol(),
                url.getHost(),
                url.getPort(),
                url.getDefaultPort(),
                url.getFile(),
                url.getPath(),
                url.getQuery(),
                url.getRef(),
                url.getUserInfo(),
                url.getAuthority(),
                url.hashCode()
        );
    }
}
