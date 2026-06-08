package dev.wonyoung.dicegame.protocol;

/**
 * 클라이언트-서버 간 주고받는 메시지의 종류.
 *
 * <p>앞부분은 클라이언트->서버 요청, 뒷부분은 서버->클라이언트 응답/푸시다.</p>
 */
public enum MessageType {

    // ===== 클라이언트 -> 서버 =====
    /** 로그온 요청 (ID 중복 검사 대상) */
    LOGIN,
    /** 로그아웃 (초기화면 복귀) */
    LOGOUT,
    /** 접속자 목록 요청 */
    LIST_USERS,
    /** 특정 상대에게 게임 신청 */
    REQUEST_GAME,
    /** 게임 신청에 대한 수락/거절 응답 */
    RESPOND_GAME,
    /** 주사위 2개의 결과 합 전송 */
    ROLL_RESULT,
    /** 게임 종료 버튼 */
    END_GAME,

    // ===== 서버 -> 클라이언트 =====
    /** 로그온 결과 + 현재 접속자 목록 */
    LOGIN_RESULT,
    /** 접속자 목록 (변경 시 브로드캐스트) */
    USER_LIST,
    /** (상대에게 푸시) 게임 참여 요청 도착 */
    GAME_REQUESTED,
    /** 상대가 이미 게임 중이라 신청 불가 */
    GAME_BUSY,
    /** 상대가 신청을 거절함 */
    GAME_DECLINED,
    /** 게임 시작 통지 (양측) */
    GAME_STARTED,
    /** 한 라운드 판정 결과 */
    ROUND_RESULT,
    /** 게임 최종 결과 */
    GAME_ENDED,
    /** 오류 통지 */
    ERROR
}
