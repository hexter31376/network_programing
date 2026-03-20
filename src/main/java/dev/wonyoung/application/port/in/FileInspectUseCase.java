package dev.wonyoung.application.port.in;

import dev.wonyoung.domain.FileInfo;

public interface FileInspectUseCase {
    FileInfo inspect(String pathStr);
}
