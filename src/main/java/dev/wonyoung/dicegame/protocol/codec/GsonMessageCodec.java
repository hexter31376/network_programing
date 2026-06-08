package dev.wonyoung.dicegame.protocol.codec;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.wonyoung.dicegame.protocol.Message;
import dev.wonyoung.dicegame.protocol.MessageType;

/**
 * Gson 기반 {@link MessageCodec} 구현.
 *
 * <p>payload record는 Gson의 {@code toJsonTree}로 {@link JsonObject}에 담아 봉투에 싣고,
 * 수신 측에서는 {@code fromJson}으로 원하는 record 타입으로 되돌린다.</p>
 */
public class GsonMessageCodec implements MessageCodec {

    private final Gson gson = new Gson();

    @Override
    public Message message(MessageType type, Object payload) {
        JsonObject payloadObject = (payload == null)
                ? new JsonObject()
                : gson.toJsonTree(payload).getAsJsonObject();
        return new Message(type, payloadObject);
    }

    @Override
    public <T> T payloadAs(Message message, Class<T> type) {
        return gson.fromJson(message.payload(), type);
    }

    @Override
    public String encode(Message message) {
        return gson.toJson(message);
    }

    @Override
    public Message decode(String json) {
        return gson.fromJson(json, Message.class);
    }
}
