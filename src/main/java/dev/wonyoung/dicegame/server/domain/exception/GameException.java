package dev.wonyoung.dicegame.server.domain.exception;

import dev.wonyoung.common.exception.BusinessException;

/**
 * 주사위 게임 서버에서 발생하는 도메인 예외.
 *
 * <p>처리 불가한 메시지처럼 정말 예외적인 상황에만 던진다.
 * 중복 ID·상대 게임 중·신청 거절 같은 정상 비즈니스 결과는 예외가 아니라
 * 푸시 메시지(GAME_BUSY 등)로 클라이언트에 알린다.</p>
 *
 * <p>던져진 예외는 {@link dev.wonyoung.common.exception.ExceptionHandler} AOP가
 * 가로채어 로깅한다.</p>
 */
public class GameException extends BusinessException {

    public GameException(String message) {
        super(message);
    }
}
