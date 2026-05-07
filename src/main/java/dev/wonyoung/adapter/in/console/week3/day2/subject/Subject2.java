package dev.wonyoung.adapter.in.console.week3.day2.subject;

import dev.wonyoung.application.port.in.week3.day2.LineNumberUseCase;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import java.util.List;

@Component
public class Subject2 {

    private static final String SOURCE_PATH = "src/main/java/dev/wonyoung/adapter/in/console/week3/day2/subject/Subject2.java";
    private static final String OUTPUT_PATH = "fileset/subject2.txt";

    private final LineNumberUseCase lineNumberUseCase;

    @Inject
    public Subject2(LineNumberUseCase lineNumberUseCase) {
        this.lineNumberUseCase = lineNumberUseCase;
    }

    public void start() {
        System.out.println("\n=== Subject2: 소스 파일 행 번호 출력 ===");
        System.out.println("대상 파일: " + SOURCE_PATH);
        System.out.println("--- 출력 시작 ---");

        List<String> numbered = lineNumberUseCase.process(SOURCE_PATH, OUTPUT_PATH);
        numbered.forEach(System.out::println);

        System.out.println("--- 출력 완료 ---");
        System.out.println("파일에 저장되었습니다: " + OUTPUT_PATH);
    }
}
