package dev.wonyoung.adapter.in.view;

import dev.wonyoung.domain.FileInfo;

import java.awt.event.ActionListener;

public interface FileInspectView {

    String getPath();
    void setPath(String path);
    String chooseFile();

    void showFileInfo(FileInfo info);
    void showError(String message);

    void bindInspectListener(ActionListener listener);
    void bindChooseListener(ActionListener listener);

    void display();
}
