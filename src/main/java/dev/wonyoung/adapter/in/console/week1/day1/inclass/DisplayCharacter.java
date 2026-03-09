package dev.wonyoung.adapter.in.console.week1.day1.inclass;

import dev.wonyoung.infrastructure.container.di.Component;

import java.io.IOException;

@Component
public class DisplayCharacter {

    /**
     * 32 ~ 126까지의 아스키코드를 출력하는 메서드입니다.
     * 각 아스키코드는 탭으로 구분되며, 8개마다 줄바꿈이 이루어집니다.
     */
    public void start() throws IOException {
        for (int i = 32; i < 127; i++) {
            System.out.write(i); // 32 ~ 126까지의 아스키코드 출력
            if (i % 8 == 7) { // 8개마다 줄바꿈
                System.out.write('\n');
            } else {
                System.out.write('\t');
            }
        }
        System.out.write('\n');
        System.out.flush();
    }
}
