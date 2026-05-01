package dev.wonyoung.domin.port.in;

import dev.wonyoung.domin.model.FileRequest;
import dev.wonyoung.domin.model.FileResponse;

public interface ServeFileUseCase {
    FileResponse serve(FileRequest request);
}
