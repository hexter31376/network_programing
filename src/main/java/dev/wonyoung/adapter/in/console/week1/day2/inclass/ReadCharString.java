package dev.wonyoung.adapter.in.console.week1.day2.inclass;

import dev.wonyoung.infrastructure.container.di.Component;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;

@Component
public class ReadCharString {

    public static final String FILESET_CHARDATA_TXT = "fileset/chardata.txt";

    public void start() {

        char ch;
        String sdata1, sdata2;

        try (
                FileInputStream fis = new FileInputStream(FILESET_CHARDATA_TXT);
                DataInputStream dis = new DataInputStream(fis)
        ) {
            sdata1 = dis.readUTF();
            sdata2 = dis.readUTF();
            ch = dis.readChar();
            System.out.println(ch);
            System.out.println(sdata1);
            System.out.println(sdata2);
        } catch (EOFException e) {
            System.err.println("파일의 끝에 도달했습니다.");
        } catch (IOException e) {
            System.err.println("파일에 데이터를 쓰는 도중 오류가 발생했습니다.");
        }
    }
}
