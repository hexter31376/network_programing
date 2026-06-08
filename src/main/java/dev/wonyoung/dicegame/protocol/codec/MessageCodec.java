package dev.wonyoung.dicegame.protocol.codec;

import dev.wonyoung.dicegame.protocol.Message;
import dev.wonyoung.dicegame.protocol.MessageType;

/**
 * 메시지와 JSON 문자열, 그리고 payload record 간 변환을 담당하는 코덱.
 *
 * <p>구현체({@link GsonMessageCodec})는 컨테이너 스캔 대상이 아니라
 * 각 {@code AppConfig}에서 {@code container.register(MessageCodec.class, ...)}로 수동 등록한다.
 * (서버/클라가 공유하는 공용 타입이라 특정 스캔 패키지에 두지 않기 위함)</p>
 */
public interface MessageCodec {

    /**
     * 타입과 payload 객체로 전송용 {@link Message}를 만든다.
     *
     * @param type    메시지 종류
     * @param payload payload record (없으면 {@code null} -> 빈 객체)
     * @return 봉투 메시지
     */
    Message message(MessageType type, Object payload);

    /**
     * 메시지의 payload를 지정한 record 타입으로 역직렬화한다.
     *
     * @param message 대상 메시지
     * @param type    변환할 payload record 클래스
     * @param <T>     payload 타입
     * @return 변환된 payload 인스턴스
     */
    <T> T payloadAs(Message message, Class<T> type);

    /**
     * 메시지를 전송용 JSON 한 줄로 직렬화한다.
     *
     * @param message 직렬화할 메시지
     * @return JSON 문자열 (개행 미포함)
     */
    String encode(Message message);

    /**
     * JSON 한 줄을 메시지로 역직렬화한다.
     *
     * @param json JSON 문자열
     * @return 복원된 메시지
     */
    Message decode(String json);
}
