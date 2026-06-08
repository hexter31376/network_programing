package dev.wonyoung.common.config;

import dev.wonyoung.common.container.Container;
import dev.wonyoung.common.exception.ExceptionHandler;
import dev.wonyoung.dicegame.client.adapter.in.server.ServerEventHandler;
import dev.wonyoung.dicegame.client.adapter.in.ui.swing.MainFrame;
import dev.wonyoung.dicegame.client.domain.port.in.ClientGameUseCase;
import dev.wonyoung.dicegame.client.domain.port.out.ServerGateway;
import dev.wonyoung.dicegame.protocol.codec.GsonMessageCodec;
import dev.wonyoung.dicegame.protocol.codec.MessageCodec;

import javax.swing.SwingUtilities;

/**
 * 주사위 게임 <b>클라이언트</b> 조립 설정.
 *
 * <p>서버와 동일하게 DI 컨테이너 + {@link ExceptionHandler} AOP를 활용하되, 스캔 범위를
 * 클라이언트 패키지({@code dev.wonyoung.dicegame.client})로 한정한다. 공용 {@link MessageCodec}은
 * {@code register}로 직접 주입한다.</p>
 *
 * <p>네트워크/도메인 계층(gateway·service·eventHandler)은 컨테이너가 조립하고,
 * Swing UI({@link MainFrame})만 EDT에서 직접 생성하여 {@link ServerEventHandler}에 연결한다.</p>
 */
public class DiceGameClientAppConfig {

    public void startApp() {
        try {
            init();
        } catch (Exception e) {
            System.err.println("[FATAL] 클라이언트 시작 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void init() throws Exception {
        Container container = new Container("dev.wonyoung.dicegame.client");
        container.addInterceptor(new ExceptionHandler());
        container.register(MessageCodec.class, new GsonMessageCodec());

        ServerGateway gateway = container.get(ServerGateway.class);
        ServerEventHandler eventHandler = container.get(ServerEventHandler.class);
        ClientGameUseCase useCase = container.get(ClientGameUseCase.class);

        // 서버 푸시 -> 이벤트 핸들러로 연결 (접속 전에 등록)
        gateway.setMessageListener(eventHandler::handle);

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(useCase);
            eventHandler.bindEventPort(frame);
            frame.setVisible(true);
        });
    }
}
