package dev.wonyoung.domain.model;

import dev.wonyoung.domain.model.enums.ContentKind;

import java.nio.charset.StandardCharsets;

/**
 * HTTP 응답 본문과 콘텐츠 분류 결과를 담는 도메인 모델.
 */
public class HttpResult {

    private final ContentKind kind; // 콘텐츠 분류 (TEXT, IMAGE 등)
    private final String contentType; // 원본 Content-Type 헤더 값
    private final byte[] rawBody; // 응답 본문 원시 바이트

    public HttpResult(ContentKind kind, String contentType, byte[] rawBody) {
        this.kind = kind;
        this.contentType = contentType;
        this.rawBody = rawBody;
    }

    public ContentKind kind() {
        return kind;
    }

    public String contentType() {
        return contentType;
    }

    /** 응답 본문의 바이트 크기를 반환한다. */
    public int byteLength() {
        return rawBody.length;
    }

    /** 응답 본문을 UTF-8 문자열로 변환해 반환한다. */
    public String textBody() {
        return new String(rawBody, StandardCharsets.UTF_8);
    }
}
