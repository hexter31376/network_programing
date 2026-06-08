package dev.wonyoung.dicegame.client.adapter.in.ui.swing;

import dev.wonyoung.dicegame.client.domain.port.in.ClientGameUseCase;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.GridLayout;

/**
 * 초기 화면. 서버 주소와 사용할 ID를 입력해 접속·로그온한다.
 */
public class LoginPanel extends JPanel {

    private final JTextField hostField = new JTextField("localhost");
    private final JTextField portField = new JTextField("5050");
    private final JTextField idField = new JTextField();

    public LoginPanel(MainFrame parent, ClientGameUseCase useCase) {
        setLayout(new GridLayout(5, 2, 8, 8));

        add(new JLabel("서버 호스트:"));
        add(hostField);
        add(new JLabel("서버 포트:"));
        add(portField);
        add(new JLabel("사용자 ID:"));
        add(idField);

        JButton connectButton = new JButton("접속");
        add(new JLabel());
        add(connectButton);

        connectButton.addActionListener(e -> attemptLogin(parent, useCase, connectButton));
    }

    private void attemptLogin(MainFrame parent, ClientGameUseCase useCase, JButton connectButton) {
        // 입력 검증은 EDT에서 즉시 수행한다.
        String host = hostField.getText().trim();
        String id = idField.getText().trim();
        if (host.isEmpty() || id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "호스트와 ID를 입력하세요.", "입력 필요", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "포트는 숫자여야 합니다.", "입력 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 소켓 접속은 블로킹이므로 EDT가 아닌 워커 스레드에서 수행한다(GUI 멈춤 방지).
        connectButton.setEnabled(false);
        new Thread(() -> {
            boolean connected = useCase.connect(host, port);
            if (connected) {
                parent.setPendingId(id);
                useCase.login(id);
                // 로그온 결과(성공/중복)는 onLoginResult 콜백에서 처리한다.
                SwingUtilities.invokeLater(() -> connectButton.setEnabled(true));
            } else {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "서버에 접속할 수 없습니다.", "접속 실패", JOptionPane.ERROR_MESSAGE);
                    connectButton.setEnabled(true);
                });
            }
        }, "client-connect").start();
    }
}
