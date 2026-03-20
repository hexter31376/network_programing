package dev.wonyoung.adapter.in;

import dev.wonyoung.adapter.in.view.FileInspectView;
import dev.wonyoung.application.port.in.FileInspectUseCase;
import dev.wonyoung.domain.FileInfo;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

@Component
public class FileInspectPresenter {

    private final FileInspectView view;
    private final FileInspectUseCase fileInspectUseCase;

    @Inject
    public FileInspectPresenter(FileInspectView view, FileInspectUseCase fileInspectUseCase) {
        this.view = view;
        this.fileInspectUseCase = fileInspectUseCase;
    }

    public void start() {
        view.display();
        view.bindChooseListener(e -> handleChoose());
        view.bindInspectListener(e -> handleInspect());
    }

    private void handleChoose() {
        String selected = view.chooseFile();
        if (selected != null) {
            view.setPath(selected);
            handleInspect();
        }
    }

    private void handleInspect() {
        String pathStr = view.getPath();
        if (pathStr == null || pathStr.isBlank()) {
            view.showError("경로를 입력하거나 파일을 선택하세요.");
            return;
        }

        try {
            FileInfo info = fileInspectUseCase.inspect(pathStr);
            view.showFileInfo(info);
        } catch (RuntimeException ex) {
            view.showError(ex.getMessage());
        }
    }
}
