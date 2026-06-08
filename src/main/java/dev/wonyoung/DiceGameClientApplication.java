package dev.wonyoung;

import dev.wonyoung.common.config.DiceGameClientAppConfig;

/**
 * 주사위 게임 <b>클라이언트</b> 진입점.
 *
 * <p>서버({@link DiceGameApplication})와 독립적으로 실행된다. {@code ./gradlew runClient}로 기동한다.</p>
 */
public class DiceGameClientApplication {

    public static void main(String[] args) {
        DiceGameClientAppConfig config = new DiceGameClientAppConfig();
        config.startApp();
    }
}
