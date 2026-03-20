package dev.wonyoung.application.port.service;

import dev.wonyoung.application.port.in.FileInspectUseCase;
import dev.wonyoung.domain.FileInfo;
import dev.wonyoung.infrastructure.container.di.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link FileInspectUseCase}의 구현체.
 * 주어진 경로의 파일 또는 디렉토리에 대한 상세 정보를 분석하여 {@link FileInfo}로 반환한다.
 */
@Component
public class FileInspectService implements FileInspectUseCase {

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    private static final String NO_PARENT            = "(없음)";
    private static final String CANONICAL_FAIL_PREFIX = "(읽기 실패: ";

    /**
     * 주어진 경로를 분석하여 파일 정보를 반환한다.
     *
     * @param pathStr 분석할 파일 또는 디렉토리의 경로 문자열
     * @return 분석된 파일 정보
     * @throws IllegalArgumentException 경로가 존재하지 않을 경우
     * @throws RuntimeException 파일 속성 읽기에 실패한 경우
     */
    @Override
    public FileInfo inspect(String pathStr) {
        Path path = Path.of(pathStr);

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("\"" + pathStr + "\" 은(는) 존재하지 않습니다.");
        }

        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);

            String name          = path.getFileName().toString();
            String originalPath  = pathStr;
            String absolutePath  = path.toAbsolutePath().toString();
            String canonicalPath = canonicalOf(path);
            Path parentPath      = path.getParent();
            String parent        = (parentPath != null) ? parentPath.toString() : NO_PARENT;
            boolean isAbsolute   = path.isAbsolute();
            boolean isHidden     = isHiddenSafe(path);
            boolean isRegular    = attrs.isRegularFile();
            boolean isDirectory  = attrs.isDirectory();
            boolean canRead      = path.toFile().canRead();
            boolean canWrite     = path.toFile().canWrite();
            boolean canExecute   = path.toFile().canExecute();
            String modifiedAt    = DT_FMT.format(attrs.lastModifiedTime().toInstant());

            if (isRegular) {
                Long wordCount = isTxtFile(name) ? countWords(path) : null;
                return new FileInfo(name, originalPath, absolutePath, canonicalPath, parent,
                        isAbsolute, isHidden, isRegular, isDirectory,
                        canRead, canWrite, canExecute,
                        modifiedAt, attrs.size(), wordCount,
                        null, null, null);
            }

            if (isDirectory) {
                AtomicInteger fileCount = new AtomicInteger();
                AtomicInteger dirCount  = new AtomicInteger();
                AtomicLong    total     = new AtomicLong();

                Files.walkFileTree(path, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes a) {
                        fileCount.incrementAndGet();
                        total.addAndGet(a.size());
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) {
                        if (!d.equals(path)) dirCount.incrementAndGet();
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException ex) {
                        return FileVisitResult.CONTINUE;
                    }
                });

                return new FileInfo(name, originalPath, absolutePath, canonicalPath, parent,
                        isAbsolute, isHidden, isRegular, isDirectory,
                        canRead, canWrite, canExecute,
                        modifiedAt, null, null,
                        fileCount.get(), dirCount.get(), total.get());
            }

            // 기타 (소켓, 디바이스 등)
            return new FileInfo(name, originalPath, absolutePath, canonicalPath, parent,
                    isAbsolute, isHidden, isRegular, isDirectory,
                    canRead, canWrite, canExecute,
                    modifiedAt, null, null,
                    null, null, null);

        } catch (IOException e) {
            throw new RuntimeException("파일 정보 읽기 실패: " + e.getMessage(), e);
        }
    }

    private static boolean isTxtFile(String name) {
        return name.toLowerCase().endsWith(".txt");
    }

    private static Long countWords(Path path) {
        try {
            String content = Files.readString(path);
            String[] tokens = content.trim().split("\\s+");
            return (tokens.length == 1 && tokens[0].isEmpty()) ? 0L : tokens.length;
        } catch (IOException e) {
            return null;
        }
    }

    private static String canonicalOf(Path path) {
        try {
            return path.toRealPath().toString();
        } catch (IOException e) {
            return CANONICAL_FAIL_PREFIX + e.getMessage() + ")";
        }
    }

    private static boolean isHiddenSafe(Path path) {
        try {
            return Files.isHidden(path);
        } catch (IOException e) {
            return false;
        }
    }
}