package dev.wonyoung.adapter.in.console.week1.day1.subject;

import dev.wonyoung.infrastructure.container.di.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class Subject3 {

    public static final int FIVE_SECOND = 5000;

    public static final String FILESET_INPUT_TXT = "fileset/input.txt";
    public static final String FILESET_OUTPUT_TXT = "fileset/output.txt";

    public void start() {
        timeCopyingFile("방법 1: read/write 반복 (byte-by-byte)", this::copyFileByByte);
        sleep();
        fileDelete();

        timeCopyingFile("방법 2: transferTo", this::copyFileByTransferTo);
        sleep();
        fileDelete();

        timeCopyingFile("방법 3: Files.copy", this::copyFileByFilesCopy);
        fileDelete();

        System.out.println();
    }

    private void timeCopyingFile(String name, Runnable task) {
        long start = System.currentTimeMillis();
        task.run();
        long end = System.currentTimeMillis();
        System.out.println("[" + name + "] 복사에 걸린 시간: " + (end - start) + " ms");
    }

    private void copyFileByByte() {
        try (
                FileInputStream fis = new FileInputStream(FILESET_INPUT_TXT);
                FileOutputStream fos = new FileOutputStream(FILESET_OUTPUT_TXT)
        ) {
            int b;
            while ((b = fis.read()) != -1) {
                fos.write(b);
            }
        } catch (IOException e) {
            System.out.println("파일 복사가 실패하였습니다 : " + e.getMessage());
        }
    }

    private void copyFileByTransferTo() {
        try (
                FileInputStream fis = new FileInputStream(FILESET_INPUT_TXT);
                FileOutputStream fos = new FileOutputStream(FILESET_OUTPUT_TXT)
        ) {
            fis.transferTo(fos);
        } catch (IOException e) {
            System.out.println("파일 복사가 실패하였습니다 : " + e.getMessage());
        }
    }

    private void copyFileByFilesCopy() {
        try {
            Files.copy(
                    Paths.get(FILESET_INPUT_TXT),
                    Paths.get(FILESET_OUTPUT_TXT),
                    StandardCopyOption.REPLACE_EXISTING
            );
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
            Files.deleteIfExists(Paths.get(FILESET_OUTPUT_TXT));
        } catch (IOException e) {
            System.out.println("파일 삭제에 실패하였습니다 : " + e.getMessage());
        }
    }
}
