package dev.wonyoung.dicegame.server.adapter.in.socket;

import dev.wonyoung.dicegame.protocol.Message;
import dev.wonyoung.dicegame.protocol.codec.MessageCodec;
import dev.wonyoung.dicegame.server.adapter.out.SocketClientNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 한 클라이언트 연결을 전담하는 워커. {@code ExecutorService}에 제출되어 별도 스레드에서 실행된다
 * (thread-per-request 모델).
 *
 * <p>소켓에서 개행으로 구분된 JSON을 한 줄씩 읽어 {@link MessageDispatcher}로 넘긴다.
 * 연결이 끊기면 세션을 정리한다.</p>
 */
public class ClientConnectionHandler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ClientConnectionHandler.class);

    private final Socket socket;
    private final MessageDispatcher dispatcher;
    private final MessageCodec codec;

    public ClientConnectionHandler(Socket socket, MessageDispatcher dispatcher, MessageCodec codec) {
        this.socket = socket;
        this.dispatcher = dispatcher;
        this.codec = codec;
    }

    @Override
    public void run() {
        ClientSession session = null;
        try (Socket connection = socket) {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8));

            session = new ClientSession(new SocketClientNotifier(writer, codec));

            String line;
            while ((line = reader.readLine()) != null) {
                handleLine(line, session);
            }
        } catch (IOException e) {
            logger.debug("연결 종료: {}", e.getMessage());
        } finally {
            if (session != null) {
                dispatcher.disconnect(session);
            }
        }
    }

    /**
     * 한 줄(메시지)을 디코드하여 처리한다. 한 메시지의 실패가 연결 전체를 끊지 않도록 격리한다.
     */
    private void handleLine(String line, ClientSession session) {
        try {
            Message message = codec.decode(line);
            if (message == null || message.type() == null) {
                return;
            }
            dispatcher.dispatch(message, session);
        } catch (RuntimeException ex) {
            logger.warn("메시지 처리 실패: {}", ex.getMessage());
        }
    }
}
