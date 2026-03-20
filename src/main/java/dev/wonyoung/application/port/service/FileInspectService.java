package dev.wonyoung.application.port.service;

import dev.wonyoung.application.port.in.FileInspectUseCase;
import dev.wonyoung.domain.FileInfo;
import dev.wonyoung.infrastructure.container.di.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class FileInspectService implements FileInspectUseCase {

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    @Override
    public FileInfo inspect(String pathStr) {
        Path path = Path.of(pathStr);

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("\"" + pathStr + "\" 은(는) 존재하지 않습니다.");
        }

        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);

            String name = path.getFileName().toString();
            String absolutePath = path.toAbsolutePath().toString();
            String canonicalPath = canonicalOf(path);
            String parent = String.valueOf(path.getParent());
            boolean isAbsolute = path.isAbsolute();
            boolean isHidden = isHiddenSafe(path);
            boolean isRegular = attrs.isRegularFile();
            boolean isDirectory = attrs.isDirectory();
            boolean isSymLink = attrs.isSymbolicLink();
            boolean canRead = path.toFile().canRead();
            boolean canWrite = path.toFile().canWrite();
            boolean canExecute = path.toFile().canExecute();
            String createdAt = DT_FMT.format(attrs.creationTime().toInstant());
            String modifiedAt = DT_FMT.format(attrs.lastModifiedTime().toInstant());
            String accessAt = DT_FMT.format(attrs.lastAccessTime().toInstant());

            if (isRegular) {
                return new FileInfo(name, absolutePath, canonicalPath, parent,
                        isAbsolute, isHidden, true, false, isSymLink,
                        canRead, canWrite, canExecute,
                        createdAt, modifiedAt, accessAt,
                        attrs.size(), null, null, null, null);
            }

            if (isDirectory) {
                int[] fileCount = {0};
                int[] dirCount  = {0};
                long[] total    = {0L};

                Files.walkFileTree(path, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes a) {
                        fileCount[0]++;
                        total[0] += a.size();
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) {
                        if (!d.equals(path)) dirCount[0]++;
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException ex) {
                        return FileVisitResult.CONTINUE;
                    }
                });

                List<String> children = new ArrayList<>();
                try (var stream = Files.list(path)) {
                    stream.sorted().forEach(p -> {
                        String type = Files.isDirectory(p) ? "[DIR] " : "[FILE]";
                        children.add(type + "  " + p.getFileName());
                    });
                }

                return new FileInfo(name, absolutePath, canonicalPath, parent,
                        isAbsolute, isHidden, false, true, isSymLink,
                        canRead, canWrite, canExecute,
                        createdAt, modifiedAt, accessAt,
                        null, fileCount[0], dirCount[0], total[0], children);
            }

            // 기타 (소켓, 디바이스 등)
            return new FileInfo(name, absolutePath, canonicalPath, parent,
                    isAbsolute, isHidden, false, false, isSymLink,
                    canRead, canWrite, canExecute,
                    createdAt, modifiedAt, accessAt,
                    null, null, null, null, null);

        } catch (IOException e) {
            throw new RuntimeException("파일 정보 읽기 실패: " + e.getMessage(), e);
        }
    }

    private static String canonicalOf(Path path) {
        try {
            return path.toRealPath().toString();
        } catch (IOException e) {
            return "(읽기 실패: " + e.getMessage() + ")";
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
