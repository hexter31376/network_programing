package dev.wonyoung.dicegame.server.adapter.out;

import dev.wonyoung.dicegame.protocol.Message;
import dev.wonyoung.dicegame.protocol.codec.MessageCodec;
import dev.wonyoung.dicegame.server.domain.port.out.ClientNotifier;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * 하나의 클라이언트 소켓으로 메시지를 푸시하는 {@link ClientNotifier} 구현.
 *
 * <p>연결마다 하나씩 생성되어 그 소켓의 출력 스트림을 감싼다(컨테이너 빈이 아니라
 * {@code ClientConnectionHandler}가 직접 생성). 여러 워커 스레드가 같은 클라이언트로
 * 동시에 푸시할 수 있으므로 {@code push}는 {@code synchronized}로 보호한다.</p>
 */
public class SocketClientNotifier implements ClientNotifier {

    private final BufferedWriter writer;
    private final MessageCodec codec;

    public SocketClientNotifier(BufferedWriter writer, MessageCodec codec) {
        this.writer = writer;
        this.codec = codec;
    }

    @Override
    public synchronized void push(Message message) {
        try {
            writer.write(codec.encode(message));
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            // 클라이언트가 끊긴 경우. 읽기 루프가 종료를 감지하여 정리한다.
        }
    }
}
