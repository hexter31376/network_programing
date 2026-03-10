package dev.wonyoung.adapter.in.console.week1.day2.inclass;

import dev.wonyoung.infrastructure.container.di.Component;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Component
public class WriteNumberData {

    public static final String FILE_DATA_NUMBER_TXT = "fileset/number.txt";

    public void start() {
        try (
                FileOutputStream fos = new FileOutputStream(FILE_DATA_NUMBER_TXT);
                DataOutputStream dos = new DataOutputStream(fos)
        ) {
            dos.writeBoolean(true);
            dos.writeDouble(989.27);
            for (int i = 1; i <= 500; i++) {
                dos.writeInt(i); // 1 부터 500까지의 정수를 파일에 저장한다.
            }
        } catch (IOException e) {
            System.err.println("파일에 데이터를 쓰는 도중 오류가 발생했습니다.");
        }
    }
}
