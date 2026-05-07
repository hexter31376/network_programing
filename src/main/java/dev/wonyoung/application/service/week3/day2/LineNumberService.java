package dev.wonyoung.application.service.week3.day2;

import dev.wonyoung.application.port.in.week3.day2.LineNumberUseCase;
import dev.wonyoung.application.port.out.week3.day2.LineNumberFileRepository;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import java.util.ArrayList;
import java.util.List;

@Component
public class LineNumberService implements LineNumberUseCase {

    private final LineNumberFileRepository lineNumberFileRepository;

    @Inject
    public LineNumberService(LineNumberFileRepository lineNumberFileRepository) {
        this.lineNumberFileRepository = lineNumberFileRepository;
    }

    @Override
    public List<String> process(String sourcePath, String outputPath) {
        List<String> lines = lineNumberFileRepository.readLines(sourcePath);
        List<String> numbered = addLineNumbers(lines);
        lineNumberFileRepository.writeLines(outputPath, numbered);
        return numbered;
    }

    private List<String> addLineNumbers(List<String> lines) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            result.add(String.format("%3d: %s", i + 1, lines.get(i)));
        }
        return result;
    }
}
