package dev.wonyoung.adapter.in.console.week1.day1.inclass;

import dev.wonyoung.infrastructure.container.di.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Component
public class StreamCopier {

    /**
     * 콘솔로부터 데이터를 읽어들이고, 읽은 데이터를 다시 콘솔에 출력하는 메서드입니다.
     * System.in과 System.out을 사용하여 데이터를 읽고 쓰며, copy 메서드를 호출하여 데이터를 복사합니다.
     */
    public void start() {
        try {
            copy(System.in, System.out);
        } catch (IOException e) {
            System.out.println("스트림으로부터 데이터를 읽을 수 없습니다");
        }
    }

    /**
     * 입력 스트림에서 데이터를 읽어들이고, 출력 스트림에 데이터를 쓰는 메서드입니다.
     * 입력 스트림과 출력 스트림을 동기화하여 데이터를 안전하게 복사하며, 버퍼를 사용하여 효율적으로 데이터를 처리합니다.
     *
     * @param in  입력 스트림
     * @param out 출력 스트림
     * @throws IOException 입출력 예외가 발생할 경우
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        int bytesRead;
        byte[] buffer = new byte[256];

        synchronized (in) {
            synchronized (out) {
                while ((bytesRead = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        }
        out.flush();
    }
}
