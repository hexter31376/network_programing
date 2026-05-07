package dev.wonyoung.adapter.in.console.week3.day2.subject;

import dev.wonyoung.application.exception.AppException;
import dev.wonyoung.application.port.in.week3.day2.FileIoUseCase;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class Subject1 {

    private final FileIoUseCase fileIoUseCase;

    @Inject
    public Subject1(FileIoUseCase fileIoUseCase) {
        this.fileIoUseCase = fileIoUseCase;
    }

    public void start() {
        System.out.println("=== Subject1: 파일 입출력 ===");
        System.out.print("저장할 문자열을 입력하세요: ");

        String input = readFromKeyboard();
        fileIoUseCase.save(input);
        System.out.println("파일에 저장되었습니다.");

        System.out.println("--- 저장된 파일 내용 ---");
        System.out.print(fileIoUseCase.load());
    }

    private String readFromKeyboard() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        try {
            return br.readLine();
        } catch (IOException e) {
            throw new AppException("키보드 입력 오류: " + e.getMessage(), e);
        }
    }
}
