package dev.wonyoung.domain.model.enums;

/**
 * HTTP 응답 본문의 콘텐츠 종류를 나타내는 열거형.
 * ContentClassifier가 Content-Type 헤더를 분석해 이 값으로 분류한다.
 */
public enum ContentKind {
    /** text/* 또는 텍스트 성격의 application/* (json, xml 등) */
    TEXT,
    /** image/* */
    IMAGE,
    /** audio/* */
    AUDIO,
    /** video/* */
    VIDEO,
    /** 위 분류에 해당하지 않는 모든 콘텐츠 */
    OTHER
}
