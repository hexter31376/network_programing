package dev.wonyoung.dicegame.client.adapter.in.ui.swing;

import dev.wonyoung.dicegame.client.domain.port.in.ClientGameUseCase;
import dev.wonyoung.dicegame.client.domain.port.out.GameEventPort;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.CardLayout;
import java.util.List;

/**
 * 메인 창. {@link CardLayout}으로 로그인 -> 로비 -> 게임 화면을 전환하며,
 * {@link GameEventPort}를 구현해 서버 푸시를 화면에 반영한다.
 *
 * <p>컨테이너 빈이 아니라 {@code DiceGameClientAppConfig}가 직접 생성한다(Swing 컴포넌트는
 * AOP 프록시 대상에서 제외). 모든 이벤트 콜백은 {@link SwingUtilities#invokeLater}로 EDT에서 처리한다.</p>
 */
public class MainFrame extends JFrame implements GameEventPort {

    private final ClientGameUseCase useCase;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    private final LoginPanel loginPanel;
    private final LobbyPanel lobbyPanel;
    private final GamePanel gamePanel;

    private String myId;
    private String pendingId;
    private String currentGameId;

    public MainFrame(ClientGameUseCase useCase) {
        super("주사위 던지기 게임");
        this.useCase = useCase;
        this.loginPanel = new LoginPanel(this, useCase);
        this.lobbyPanel = new LobbyPanel(this, useCase);
        this.gamePanel = new GamePanel(this, useCase);

        cards.add(loginPanel, "login");
        cards.add(lobbyPanel, "lobby");
        cards.add(gamePanel, "game");

        setContentPane(cards);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(480, 440);
        setLocationRelativeTo(null);
        showLogin();
    }

    // ===== 화면 전환 / 상태 =====

    void showLogin() {
        cardLayout.show(cards, "login");
    }

    void showLobby() {
        cardLayout.show(cards, "lobby");
    }

    void showGame() {
        cardLayout.show(cards, "game");
    }

    void setPendingId(String pendingId) {
        this.pendingId = pendingId;
    }

    String getCurrentGameId() {
        return currentGameId;
    }

    // ===== GameEventPort (수신 스레드 -> EDT) =====

    @Override
    public void onLoginResult(boolean success, String reason, List<String> users) {
        SwingUtilities.invokeLater(() -> {
            if (success) {
                myId = pendingId;
                lobbyPanel.setMyId(myId);
                lobbyPanel.updateUsers(users, myId);
                showLobby();
            } else {
                JOptionPane.showMessageDialog(this, reason, "로그온 실패", JOptionPane.WARNING_MESSAGE);
                useCase.disconnect();
            }
        });
    }

    @Override
    public void onUserList(List<String> users) {
        SwingUtilities.invokeLater(() -> lobbyPanel.updateUsers(users, myId));
    }

    @Override
    public void onGameRequested(String fromId) {
        SwingUtilities.invokeLater(() -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    fromId + "님이 게임을 신청했습니다. 수락하시겠습니까?",
                    "게임 신청", JOptionPane.YES_NO_OPTION);
            useCase.respondGame(fromId, choice == JOptionPane.YES_OPTION);
        });
    }

    @Override
    public void onGameBusy(String targetId) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                targetId + "님은 이미 게임 중입니다. 다른 사용자를 선택하세요.",
                "신청 불가", JOptionPane.INFORMATION_MESSAGE));
    }

    @Override
    public void onGameDeclined(String byId) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                byId + "님이 게임 신청을 거절했습니다.",
                "신청 거절", JOptionPane.INFORMATION_MESSAGE));
    }

    @Override
    public void onGameStarted(String gameId, String opponentId) {
        SwingUtilities.invokeLater(() -> {
            currentGameId = gameId;
            gamePanel.startGame(opponentId);
            showGame();
        });
    }

    @Override
    public void onRoundResult(int yourSum, int oppSum, String outcome) {
        SwingUtilities.invokeLater(() -> gamePanel.showRound(yourSum, oppSum, outcome));
    }

    @Override
    public void onGameEnded(int wins, int losses, int draws, String finalOutcome) {
        SwingUtilities.invokeLater(() -> {
            currentGameId = null;
            String summary = String.format("게임 종료!%n전적: %d승 %d패 %d무 -> %s",
                    wins, losses, draws, koreanOutcome(finalOutcome));
            JOptionPane.showMessageDialog(this, summary, "게임 결과", JOptionPane.INFORMATION_MESSAGE);
            showLobby();
            useCase.refreshUsers();
        });
    }

    @Override
    public void onError(String code, String message) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                message, "오류 (" + code + ")", JOptionPane.ERROR_MESSAGE));
    }

    private String koreanOutcome(String outcome) {
        return switch (outcome) {
            case "WIN" -> "승리";
            case "LOSE" -> "패배";
            default -> "무승부";
        };
    }
}
