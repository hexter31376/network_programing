package dev.wonyoung.infrastructure.adapter.subject3;

import dev.wonyoung.domin.model.CompressRequest;
import dev.wonyoung.domin.model.CompressResult;
import dev.wonyoung.domin.port.in.CompressUseCase;
import dev.wonyoung.infrastructure.container.di.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

@Component
public class CompressService implements CompressUseCase {

    @Override
    public CompressResult compress(CompressRequest request) {
        try {
            var baos = new ByteArrayOutputStream();
            try (var gzip = new GZIPOutputStream(baos)) {
                gzip.write(request.content());
            }
            return new CompressResult(request.fileName() + ".gz", baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("압축 실패: " + e.getMessage(), e);
        }
    }
}
