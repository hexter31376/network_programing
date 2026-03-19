package dev.wonyoung.adapter.in.console.week2.day1.subject;

import dev.wonyoung.infrastructure.container.di.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;

@Component
public class Subject1 {

    public void start() {
        try (
                FileAndConsoleOutputStream out = new FileAndConsoleOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                Scanner scanner = new Scanner(System.in)
        ) {
            System.out.print("[Subject1] 문자열을 입력하세요: ");
            String input = scanner.nextLine();
            writer.write(input);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("입출력 오류: " + e.getMessage());
        }
    }
}
