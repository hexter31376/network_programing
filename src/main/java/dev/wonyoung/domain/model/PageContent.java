package dev.wonyoung.domain.model;

import dev.wonyoung.domain.model.enums.ContentKind;

/**
 * Subject3용 페이지 응답 결과 도메인 모델.
 *
 * @param kind        콘텐츠 분류 (TEXT / IMAGE / OTHER 등)
 * @param headersText 전체 헤더 정보를 포매팅한 문자열 (상태 라인 포함)
 * @param body        응답 본문 바이트 배열
 * @param withinTenDays Last-Modified 기준 10일 이내 수정 여부
 */
public record PageContent(
        ContentKind kind,
        String headersText,
        byte[] body,
        boolean withinTenDays
) {}
