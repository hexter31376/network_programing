package dev.wonyoung.adapter.in.view;

import dev.wonyoung.domain.FileInfo;
import dev.wonyoung.infrastructure.container.di.Component;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionListener;

@Component
public class FileInspectViewImpl extends JFrame implements FileInspectView {

    private static final long KB = 1024L;
    private static final long MB = 1024L * KB;

    private final JTextField pathField;
    private final JTextArea  resultArea;
    private final JButton    chooseButton;
    private final JButton    inspectButton;
    private final JLabel     statusLabel;

    public FileInspectViewImpl() {
        super("File Inspector");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(620, 560);
        setLocationRelativeTo(null);

        pathField = new JTextField();
        chooseButton = new JButton("파일 선택");
        inspectButton = new JButton("조회");

        JPanel topPanel = new JPanel(new BorderLayout(6, 0));
        topPanel.setBorder(new EmptyBorder(8, 8, 4, 8));
        topPanel.add(pathField, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        btnPanel.add(chooseButton);
        btnPanel.add(inspectButton);
        topPanel.add(btnPanel, BorderLayout.EAST);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        resultArea.setMargin(new Insets(8, 10, 8, 10));

        statusLabel = new JLabel("파일 또는 디렉토리를 선택하세요.");
        statusLabel.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                new EmptyBorder(4, 10, 4, 10)));
        statusLabel.setForeground(Color.DARK_GRAY);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    @Override
    public String getPath() {
        return pathField.getText().trim();
    }

    @Override
    public void setPath(String path) {
        pathField.setText(path);
    }

    @Override
    public String chooseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setDialogTitle("파일 또는 디렉토리 선택");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    @Override
    public void showFileInfo(FileInfo info) {
        resultArea.setText(buildReport(info));
        resultArea.setCaretPosition(0);
        statusLabel.setText("조회 완료: " + info.absolutePath());
        statusLabel.setForeground(new Color(0, 120, 0));
    }

    @Override
    public void showError(String message) {
        resultArea.setText(message);
        statusLabel.setText("오류");
        statusLabel.setForeground(Color.RED.darker());
    }

    @Override
    public void bindInspectListener(ActionListener listener) {
        inspectButton.addActionListener(listener);
        pathField.addActionListener(listener);
    }

    @Override
    public void bindChooseListener(ActionListener listener) {
        chooseButton.addActionListener(listener);
    }

    @Override
    public void display() {
        setVisible(true);
    }

    private String buildReport(FileInfo info) {
        var sb = new StringBuilder();

        sb.append("----------------------------------------------------\n");
        sb.append("  기본 정보\n");
        sb.append("----------------------------------------------------\n");
        sb.append(row("이름", info.name()));
        sb.append(row("경로 (Path)", info.absolutePath()));
        sb.append(row("정규 경로 (Canonical)", info.canonicalPath()));
        sb.append(row("부모 디렉토리", info.parent()));
        sb.append(row("절대경로 여부", yesNo(info.absolute())));
        sb.append(row("숨김 파일 여부", yesNo(info.hidden())));

        sb.append("\n");
        sb.append("----------------------------------------------------\n");
        sb.append("  유형\n");
        sb.append("----------------------------------------------------\n");
        sb.append(row("일반 파일", yesNo(info.regularFile())));
        sb.append(row("디렉토리", yesNo(info.directory())));
        sb.append(row("심볼릭 링크", yesNo(info.symbolicLink())));

        sb.append("\n");
        sb.append("----------------------------------------------------\n");
        sb.append("  권한\n");
        sb.append("----------------------------------------------------\n");
        sb.append(row("읽기 (canRead)", yesNo(info.canRead())));
        sb.append(row("쓰기 (canWrite)", yesNo(info.canWrite())));
        sb.append(row("실행 (canExecute)", yesNo(info.canExecute())));

        sb.append("\n");
        sb.append("----------------------------------------------------\n");
        sb.append("  시간\n");
        sb.append("----------------------------------------------------\n");
        sb.append(row("생성일", info.createdAt()));
        sb.append(row("마지막 수정", info.lastModifiedAt()));
        sb.append(row("마지막 접근", info.lastAccessAt()));

        if (info.regularFile()) {
            long size = info.size();
            sb.append("\n");
            sb.append("----------------------------------------------------\n");
            sb.append("  파일 크기\n");
            sb.append("----------------------------------------------------\n");
            sb.append(row("크기 (Bytes)", size + " bytes"));
            sb.append(row("크기 (KB)", String.format("%.2f KB", (double) size / KB)));
            sb.append(row("크기 (MB)", String.format("%.4f MB", (double) size / MB)));
        }

        if (info.directory()) {
            long total = info.totalSize();
            sb.append("\n");
            sb.append("----------------------------------------------------\n");
            sb.append("  디렉토리 분석\n");
            sb.append("----------------------------------------------------\n");
            sb.append(row("파일 개수", info.fileCount() + " 개"));
            sb.append(row("디렉토리 개수", info.dirCount()  + " 개"));
            sb.append(row("총 용량 (Bytes)", total + " bytes"));
            sb.append(row("총 용량 (KB)", String.format("%.2f KB",  (double) total / KB)));
            sb.append(row("총 용량 (MB)", String.format("%.4f MB",  (double) total / MB)));

            sb.append("\n");
            sb.append("----------------------------------------------------\n");
            sb.append("  하위 항목 목록 (직접 자식)\n");
            sb.append("----------------------------------------------------\n");
            if (info.children().isEmpty()) {
                sb.append("  (비어 있는 디렉토리)\n");
            } else {
                info.children().forEach(c -> sb.append("  ").append(c).append("\n"));
            }
        }

        return sb.toString();
    }

    private static String row(String label, String value) {
        return String.format("  %-22s : %s%n", label, value);
    }

    private static String yesNo(boolean v) {
        return v ? "예 (true)" : "아니오 (false)";
    }
}
