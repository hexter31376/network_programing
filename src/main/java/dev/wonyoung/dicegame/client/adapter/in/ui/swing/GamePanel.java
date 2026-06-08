package dev.wonyoung.dicegame.client.adapter.in.ui.swing;

import dev.wonyoung.dicegame.client.domain.port.in.ClientGameUseCase;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;

/**
 * 게임 화면. 주사위를 굴려 결과를 서버로 보내고, 라운드/최종 결과를 표시한다.
 */
public class GamePanel extends JPanel {

    private final JLabel opponentLabel = new JLabel("상대: -");
    private final JLabel diceLabel = new JLabel("내 주사위: -");
    private final JLabel scoreLabel = new JLabel("전적: 0승 0패 0무");
    private final JLabel statusLabel = new JLabel(" ");
    private final JTextArea logArea = new JTextArea();
    private final JButton rollButton = new JButton("주사위 굴리기");

    private int wins;
    private int losses;
    private int draws;

    public GamePanel(MainFrame parent, ClientGameUseCase useCase) {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(opponentLabel);
        info.add(diceLabel);
        info.add(scoreLabel);
        info.add(statusLabel);
        add(info, BorderLayout.NORTH);

        logArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createTitledBorder("라운드 기록"));
        add(scroll, BorderLayout.CENTER);

        JButton endButton = new JButton("게임 종료");
        JPanel buttons = new JPanel();
        buttons.add(rollButton);
        buttons.add(endButton);
        add(buttons, BorderLayout.SOUTH);

        rollButton.addActionListener(e -> {
            int[] dice = useCase.rollDice(parent.getCurrentGameId());
            if (dice == null) {
                return;
            }
            diceLabel.setText(String.format("내 주사위: %d, %d (합 %d)", dice[0], dice[1], dice[0] + dice[1]));
            rollButton.setEnabled(false);
            statusLabel.setText("상대의 결과를 기다리는 중...");
        });
        endButton.addActionListener(e -> useCase.endGame(parent.getCurrentGameId()));
    }

    /**
     * 새 게임 시작 시 화면을 초기화한다.
     *
     * @param opponentId 상대 ID
     */
    void startGame(String opponentId) {
        opponentLabel.setText("상대: " + opponentId);
        wins = 0;
        losses = 0;
        draws = 0;
        updateScore();
        logArea.setText("");
        diceLabel.setText("내 주사위: -");
        statusLabel.setText("주사위를 굴려 게임을 시작하세요.");
        rollButton.setEnabled(true);
    }

    /**
     * 한 라운드 결과를 반영한다.
     *
     * @param yourSum 내 합
     * @param oppSum  상대 합
     * @param outcome 내 관점 결과 (WIN/LOSE/DRAW)
     */
    void showRound(int yourSum, int oppSum, String outcome) {
        String label;
        switch (outcome) {
            case "WIN" -> {
                wins++;
                label = "승";
            }
            case "LOSE" -> {
                losses++;
                label = "패";
            }
            default -> {
                draws++;
                label = "무";
            }
        }
        updateScore();
        logArea.append(String.format("내 합 %d  vs  상대 합 %d  ->  %s%n", yourSum, oppSum, label));
        statusLabel.setText("이번 라운드: " + label + " — 계속 굴리거나 종료하세요.");
        rollButton.setEnabled(true);
    }

    private void updateScore() {
        scoreLabel.setText(String.format("전적: %d승 %d패 %d무", wins, losses, draws));
    }
}
