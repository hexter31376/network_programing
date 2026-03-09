package dev.wonyoung.adapter.in.console.week1.day1.subject;

import dev.wonyoung.infrastructure.container.di.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class Subject2 {

    public static final int BYTE_256 = 256;
    public static final int KILOBYTE_1 = 1024;
    public static final int KILOBYTE_8 = 8192;
    public static final int KILOBYTE_64 = 65536;
    public static final int FIVE_SECOND = 5000;

    public static final String FILESET_INPUT_TXT = "fileset/input.txt";
    public static final String FILESET_OUTPUT_TXT = "fileset/output.txt";

    public void start() {
        int[] bufferSizes = {BYTE_256, KILOBYTE_1, KILOBYTE_8, KILOBYTE_64};

        for (int bufferSize : bufferSizes) {
            timeCopyingFile("버퍼 크기 " + bufferSize + " bytes", () -> copyFile(bufferSize));
            sleep();
            fileDelete();
        }

        System.out.println();
    }

    private void timeCopyingFile(String name, Runnable task) {
        long startTime = System.currentTimeMillis();
        task.run();
        long endTime = System.currentTimeMillis();
        System.out.println("[" + name + "] 파일 복사에 걸린 시간: " + (endTime - startTime) + " ms");
    }

    private void copyFile (int bufferSize) {
        try (
                FileInputStream fis = new FileInputStream(FILESET_INPUT_TXT);
                FileOutputStream fos = new FileOutputStream(FILESET_OUTPUT_TXT)
        ) {
            byte[] buffer = new byte[bufferSize];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            System.out.println("파일 복사가 실패하였습니다 : " + e.getMessage());
        }
    }

    private void sleep() {
        try {
            Thread.sleep(FIVE_SECOND);
        } catch (InterruptedException e) {
            System.out.println("스레드가 인터럽트되었습니다 : " + e.getMessage());
        }
    }

    private void fileDelete() {
        try {
            Files.deleteIfExists(Paths.get("fileset/output.txt"));
        } catch (IOException e) {
            System.out.println("파일 삭제에 실패하였습니다 : " + e.getMessage());
        }
    }
}
