package dev.wonyoung.domain.service;

import dev.wonyoung.domain.model.enums.ContentKind;
import dev.wonyoung.infrastructure.container.di.Component;

import java.util.Set;

/**
 * Content-Type 문자열을 분석해 콘텐츠 종류(ContentKind)로 분류하는 도메인 서비스.
 * <p>
 * MIME 타입의 type/subtype 구조를 파싱하고,
 * application/* 중 텍스트 성격을 가진 서브타입 목록을 별도로 관리한다.
 * </p>
 */
@Component
public class ContentClassifier {

    /**
     * 텍스트로 취급할 application/* 서브타입 목록.
     * 이 목록에 포함(contains)되는 subtype은 TEXT로 분류한다.
     */
    private static final Set<String> TEXTUAL_APPLICATION_SUBTYPES = Set.of(
            "json", "xml", "javascript", "x-www-form-urlencoded",
            "xhtml+xml", "ld+json", "graphql"
    );

    /**
     * Content-Type 헤더 값을 받아 응답 본문의 종류를 분류한다.
     * <p>예: "text/html; charset=utf-8" -> TEXT</p>
     */
    public ContentKind classify(String contentType) {
        String mime = extractMime(contentType);
        String type = extractType(mime);
        String subtype = extractSubtype(mime);

        if (type == null) return ContentKind.OTHER;

        return classifyByType(type, subtype);
    }

    private String extractMime(String contentType) {
        return contentType.split(";", 2)[0].trim().toLowerCase();
    }

    private String extractType(String mime) {
        int slash = mime.indexOf('/');
        if (slash < 0) return null;
        return mime.substring(0, slash);
    }

    private String extractSubtype(String mime) {
        int slash = mime.indexOf('/');
        if (slash < 0) return "";
        return mime.substring(slash + 1);
    }

    private ContentKind classifyByType(String type, String subtype) {
        return switch (type) {
            case "text" -> ContentKind.TEXT;
            case "image" -> ContentKind.IMAGE;
            case "audio" -> ContentKind.AUDIO;
            case "video" -> ContentKind.VIDEO;
            case "application" -> classifyApplicationSubtype(subtype);
            default -> ContentKind.OTHER;
        };
    }

    private ContentKind classifyApplicationSubtype(String subtype) {
        boolean isTextual = TEXTUAL_APPLICATION_SUBTYPES.stream()
                .anyMatch(subtype::contains);
        return isTextual ? ContentKind.TEXT : ContentKind.OTHER;
    }
}
