package dev.wonyoung.common.config;

import dev.wonyoung.common.container.Container;
import dev.wonyoung.common.exception.ExceptionHandler;
import dev.wonyoung.dicegame.protocol.codec.GsonMessageCodec;
import dev.wonyoung.dicegame.protocol.codec.MessageCodec;
import dev.wonyoung.dicegame.server.adapter.in.socket.TcpServerBootstrap;

/**
 * 주사위 게임 <b>서버</b> 조립 설정.
 *
 * <p>DI 컨테이너를 서버 패키지({@code dev.wonyoung.dicegame.server})로만 스캔하여
 * 클라이언트(Swing) 클래스가 서버 JVM에 로드되지 않게 한다. 서버/클라가 공유하는
 * {@link MessageCodec}은 스캔 대상이 아니므로 {@code register}로 직접 주입한다.</p>
 *
 * <p>{@link ExceptionHandler} AOP 인터셉터를 등록하여, 빈의 메서드에서 발생한 예외가
 * 자동으로 로깅되도록 한다.</p>
 */
public class DiceGameAppConfig {

    public void startApp() {
        try {
            init();
        } catch (Exception e) {
            System.err.println("[FATAL] 애플리케이션 시작 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void init() throws Exception {
        Container container = new Container("dev.wonyoung.dicegame.server");
        container.addInterceptor(new ExceptionHandler());
        container.register(MessageCodec.class, new GsonMessageCodec());

        TcpServerBootstrap server = container.get(TcpServerBootstrap.class);
        server.start();
    }
}
