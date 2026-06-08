package dev.wonyoung.dicegame.client.adapter.in.ui.swing;

import dev.wonyoung.dicegame.client.domain.port.in.ClientGameUseCase;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.util.List;

/**
 * 로비 화면. 접속자 목록에서 상대를 골라 게임을 신청한다.
 */
public class LobbyPanel extends JPanel {

    private final JLabel meLabel = new JLabel("내 ID: -");
    private final DefaultListModel<String> userListModel = new DefaultListModel<>();
    private final JList<String> userList = new JList<>(userListModel);

    public LobbyPanel(MainFrame parent, ClientGameUseCase useCase) {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        meLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        add(meLabel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(userList);
        scroll.setBorder(BorderFactory.createTitledBorder("접속자 목록"));
        add(scroll, BorderLayout.CENTER);

        JButton requestButton = new JButton("게임 신청");
        JButton refreshButton = new JButton("새로고침");
        JButton logoutButton = new JButton("로그아웃");

        JPanel buttons = new JPanel();
        buttons.add(requestButton);
        buttons.add(refreshButton);
        buttons.add(logoutButton);
        add(buttons, BorderLayout.SOUTH);

        requestButton.addActionListener(e -> {
            String selected = userList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "상대를 선택하세요.", "선택 필요", JOptionPane.WARNING_MESSAGE);
                return;
            }
            useCase.requestGame(selected);
        });
        refreshButton.addActionListener(e -> useCase.refreshUsers());
        logoutButton.addActionListener(e -> {
            useCase.logout();
            useCase.disconnect();
            userListModel.clear();
            parent.showLogin();
        });
    }

    void setMyId(String myId) {
        meLabel.setText("내 ID: " + myId);
    }

    /**
     * 접속자 목록을 갱신한다(자기 자신은 제외).
     *
     * @param users 전체 접속자 목록
     * @param myId  내 ID
     */
    void updateUsers(List<String> users, String myId) {
        userListModel.clear();
        for (String user : users) {
            if (!user.equals(myId)) {
                userListModel.addElement(user);
            }
        }
    }
}
