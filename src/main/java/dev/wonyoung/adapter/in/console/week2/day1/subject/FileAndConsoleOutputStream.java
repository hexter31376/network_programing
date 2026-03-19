package dev.wonyoung.adapter.in.console.week2.day1.subject;

import dev.wonyoung.infrastructure.container.di.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Component
public class FileAndConsoleOutputStream extends OutputStream {

    public static final String FILESET_OUTPUT_TXT = "fileset/output.txt";
    private final OutputStream fileOutputStream;
    private final OutputStream consoleOutputStream;

    public FileAndConsoleOutputStream() throws IOException {
        this.fileOutputStream = new FileOutputStream(FILESET_OUTPUT_TXT, true);
        this.consoleOutputStream = System.out;
    }

    @Override
    public void write(int b) throws IOException {
        fileOutputStream.write(b);
        consoleOutputStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        fileOutputStream.write(b);
        consoleOutputStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        fileOutputStream.write(b, off, len);
        consoleOutputStream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        fileOutputStream.flush();
        consoleOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
        fileOutputStream.close();
    }
}
