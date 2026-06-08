package dev.wonyoung.dicegame.protocol;

import com.google.gson.JsonObject;

/**
 * 클라이언트-서버 간 전송되는 메시지 봉투(envelope).
 *
 * <p>{@code type}으로 메시지 종류를 구분하고, 실제 데이터는 {@code payload}(JSON 객체)에 담는다.
 * payload의 구체 스키마는 {@code dto} 패키지의 record들이 정의하며,
 * {@link dev.wonyoung.dicegame.protocol.codec.MessageCodec}을 통해 record ↔ payload로 변환한다.</p>
 *
 * <p>전송 시 한 줄(개행 종료) JSON으로 직렬화된다. 예:</p>
 * <pre>{@code {"type":"LOGIN","payload":{"userId":"alice"}} }</pre>
 *
 * @param type    메시지 종류
 * @param payload 메시지 본문 (없으면 빈 객체)
 */
public record Message(MessageType type, JsonObject payload) {
}
