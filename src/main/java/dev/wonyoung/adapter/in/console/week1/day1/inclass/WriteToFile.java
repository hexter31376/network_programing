package dev.wonyoung.adapter.in.console.week1.day1.inclass;

import dev.wonyoung.infrastructure.container.di.Component;

import java.io.FileOutputStream;
import java.io.IOException;

@Component
public class WriteToFile {

    public void start() {
        try (FileOutputStream fout = new FileOutputStream("example1_9.txt")) {
            int bytesRead;
            byte[] buffer = new byte[256];

            while ((bytesRead = System.in.read(buffer)) >= 0) {
                fout.write(buffer, 0, bytesRead);
            }

        } catch (IOException e) {
            System.err.println("스트림으로부터 데이터를 읽을 수 없습니다.");
        }
    }
}
