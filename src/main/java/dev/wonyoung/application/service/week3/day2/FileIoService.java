package dev.wonyoung.application.service.week3.day2;

import dev.wonyoung.application.port.in.week3.day2.FileIoUseCase;
import dev.wonyoung.application.port.out.week3.day2.FileIoRepository;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

@Component
public class FileIoService implements FileIoUseCase {

    private static final String FILE_PATH = "fileset/subject1.txt";

    private final FileIoRepository fileIoRepository;

    @Inject
    public FileIoService(FileIoRepository fileIoRepository) {
        this.fileIoRepository = fileIoRepository;
    }

    @Override
    public void save(String text) {
        fileIoRepository.write(FILE_PATH, text);
    }

    @Override
    public String load() {
        return fileIoRepository.read(FILE_PATH);
    }
}
