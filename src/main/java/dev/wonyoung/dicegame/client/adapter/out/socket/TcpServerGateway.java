package dev.wonyoung.dicegame.client.adapter.out.socket;

import dev.wonyoung.common.container.di.Component;
import dev.wonyoung.common.container.di.Inject;
import dev.wonyoung.dicegame.client.domain.port.out.ServerGateway;
import dev.wonyoung.dicegame.protocol.Message;
import dev.wonyoung.dicegame.protocol.codec.MessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * TCP 소켓 기반 {@link ServerGateway} 구현.
 *
 * <p>접속 시 별도(데몬) 스레드에서 서버 푸시를 한 줄씩 읽어 리스너로 전달한다.
 * 읽기 루프는 {@code private} 메서드로 두어 AOP 프록시 가로채기를 거치지 않게 한다.</p>
 */
@Component
public class TcpServerGateway implements ServerGateway {

    private static final Logger logger = LoggerFactory.getLogger(TcpServerGateway.class);

    private final MessageCodec codec;

    private Socket socket;
    private BufferedWriter writer;
    private Consumer<Message> listener;
    private volatile boolean running;

    /**
     * 송신 전용 단일 스레드. UI(EDT) 호출자가 소켓 write로 블로킹되지 않도록
     * 모든 전송을 이 스레드에서 순차 처리한다(EDT 분리).
     */
    private final ExecutorService writerPool = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "server-writer");
        thread.setDaemon(true);
        return thread;
    });

    @Inject
    public TcpServerGateway(MessageCodec codec) {
        this.codec = codec;
    }

    @Override
    public void setMessageListener(Consumer<Message> listener) {
        this.listener = listener;
    }

    @Override
    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            running = true;
            Thread readerThread = new Thread(() -> readLoop(reader), "server-reader");
            readerThread.setDaemon(true);
            readerThread.start();
            return true;
        } catch (IOException e) {
            logger.warn("서버 접속 실패 {}:{} — {}", host, port, e.getMessage());
            return false;
        }
    }

    /**
     * 서버 푸시를 계속 읽어 리스너로 전달한다. (별도 스레드에서 실행)
     */
    private void readLoop(BufferedReader reader) {
        try {
            String line;
            while (running && (line = reader.readLine()) != null) {
                Message message = codec.decode(line);
                if (message != null && listener != null) {
                    listener.accept(message);
                }
            }
        } catch (IOException e) {
            logger.debug("서버 연결 종료: {}", e.getMessage());
        }
    }

    @Override
    public void send(Message message) {
        // EDT에서 호출되어도 즉시 반환한다. 실제 소켓 write는 writer 스레드에서 수행.
        writerPool.submit(() -> writeLine(message));
    }

    /**
     * 실제 소켓 전송. (writer 스레드에서만 호출되어 직렬화됨)
     */
    private void writeLine(Message message) {
        if (writer == null) {
            return;
        }
        try {
            writer.write(codec.encode(message));
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            logger.warn("메시지 전송 실패: {}", e.getMessage());
        }
    }

    @Override
    public void close() {
        running = false;
        writerPool.shutdownNow();
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            logger.debug("연결 종료 중 오류: {}", e.getMessage());
        }
    }
}
