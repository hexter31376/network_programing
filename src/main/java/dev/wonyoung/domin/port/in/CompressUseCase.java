package dev.wonyoung.domin.port.in;

import dev.wonyoung.domin.model.CompressRequest;
import dev.wonyoung.domin.model.CompressResult;

public interface CompressUseCase {
    CompressResult compress(CompressRequest request);
}
