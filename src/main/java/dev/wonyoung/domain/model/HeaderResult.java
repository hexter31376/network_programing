package dev.wonyoung.domain.model;

import dev.wonyoung.domain.model.enums.ContentKind;

import java.util.List;
import java.util.Map;

/**
 * HTTP 응답 헤더 전체와 콘텐츠 분류 결과를 담는 도메인 모델.
 *
 * @param statusLine  응답 상태 라인 (예: "HTTP/1.1 200 OK")
 * @param headers     헤더 이름 → 값 목록 (키는 null 가능 = 상태 라인 항목)
 * @param contentKind 헤더의 Content-Type을 분석한 콘텐츠 분류
 */
public record HeaderResult(
        String statusLine,
        Map<String, List<String>> headers,
        ContentKind contentKind
) {}
