package dev.wonyoung.adapter.in.console.week1.day1.subject;

import dev.wonyoung.infrastructure.container.di.Component;

import java.util.stream.IntStream;

@Component
public class Subject1 {

    private static final int ASCII_START = 32;
    private static final int ASCII_END = 126;
    private static final int COLS = 8;
    private static final int TOTAL = ASCII_END - ASCII_START + 1;
    private static final int ROWS = (TOTAL + COLS - 1) / COLS;

    public void start() {
        IntStream.range(0, ROWS).forEach(row -> { // 0부터 ROWS-1까지 반복 및 행 번호 스트림 생성
            System.out.printf("line%2d: ", row + 1); // 각 행의 번호를 출력
            IntStream.range(0, COLS) // 0부터 COLS-1까지 반복 및 열 번호 스트림 생성
                    .map(col -> ASCII_START + row * COLS + col) // 각 열에 해당하는 아스키 코드 계산
                    .filter(code -> code <= ASCII_END) // 아스키 코드가 ASCII_END를 초과하지 않는 경우에만 필터링
                    .forEach(code -> System.out.printf("%c\t", (char) code)); // 각 아스키 코드를 문자로 변환하여 출력
            System.out.println(); // 각 행이 끝날 때마다 줄바꿈
        });

        System.out.println();
    }
}
