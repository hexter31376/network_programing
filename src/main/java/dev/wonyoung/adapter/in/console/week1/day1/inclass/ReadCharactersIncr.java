package dev.wonyoung.adapter.in.console.week1.day1.inclass;

import dev.wonyoung.infrastructure.container.di.Component;

import java.io.IOException;

@Component
public class ReadCharactersIncr {

    private int size;
    private int bufferSize;
    private byte[] buffer;

    public ReadCharactersIncr() {
        size = 0;
        bufferSize = 80;
        buffer = new byte[bufferSize];
    }

    /**
     * 콘솔로부터 데이터를 읽어들이는 메서드입니다.
     * System.in.read() 메서드를 사용하여 데이터를 읽어들이며, 버퍼가 가득 찰 때마다 버퍼 크기를 증가시킵니다.
     * 읽은 데이터는 콘솔에 출력됩니다.
     */
    public void start() {
        try {
            int dataRead;

            while ((dataRead = System.in.read(buffer, size, bufferSize - size)) >= 0) {
                size += dataRead;

                if (size >= bufferSize) {
                    increaseBufferSize();
                }
            }
            System.out.write(buffer, 0, size);
            System.out.flush();
        } catch (IOException e) {
            System.out.println("스트림으로부터 데이터를 읽을 수 없습니다.");
        }
    }

    /**
     * 버퍼 크기를 증가시키는 메서드입니다.
     * 현재 버퍼 크기에 80을 더하여 새로운 버퍼를 생성하고, 기존 버퍼의 내용을 새로운 버퍼로 복사합니다.
     */
    private void increaseBufferSize () {
        bufferSize += 80;
        byte[] newBuffer = new byte[bufferSize];
        System.arraycopy(buffer, 0, newBuffer, 0, size);
        buffer = newBuffer;
    }
}
