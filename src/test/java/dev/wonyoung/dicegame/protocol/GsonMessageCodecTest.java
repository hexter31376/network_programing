package dev.wonyoung.dicegame.protocol;

import dev.wonyoung.dicegame.protocol.codec.GsonMessageCodec;
import dev.wonyoung.dicegame.protocol.codec.MessageCodec;
import dev.wonyoung.dicegame.protocol.dto.LoginPayload;
import dev.wonyoung.dicegame.protocol.dto.RollResultPayload;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GsonMessageCodecTest {

    private final MessageCodec codec = new GsonMessageCodec();

    @Test
    void payload가_담긴_메시지를_JSON으로_왕복한다() {
        Message original = codec.message(MessageType.LOGIN, new LoginPayload("alice"));

        String json = codec.encode(original);
        Message restored = codec.decode(json);

        assertEquals(MessageType.LOGIN, restored.type());
        LoginPayload payload = codec.payloadAs(restored, LoginPayload.class);
        assertEquals("alice", payload.userId());
    }

    @Test
    void 주사위_배열_payload도_보존된다() {
        Message original = codec.message(MessageType.ROLL_RESULT,
                new RollResultPayload("game-1", new int[]{3, 5}, 8));

        Message restored = codec.decode(codec.encode(original));
        RollResultPayload payload = codec.payloadAs(restored, RollResultPayload.class);

        assertEquals("game-1", payload.gameId());
        assertArrayEquals(new int[]{3, 5}, payload.dice());
        assertEquals(8, payload.sum());
    }
}
