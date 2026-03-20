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

    private static final String NO_PARENT        = "(없음)";
    private static final String DIR_PREFIX        = "[DIR] ";
    private static final String FILE_PREFIX       = "[FILE]";
    private static final String CHILDREN_INDENT   = "  ";
    private static final String CANONICAL_FAIL_PREFIX = "(읽기 실패: ";

    /**
     * 주어진 경로를 분석하여 파일 정보를 반환한다.
     * <p>
     * 경로 유형에 따라 다음과 같이 동작한다:
     * <ul>
     *   <li>일반 파일: 크기, 권한, 시간 정보를 포함한 {@link FileInfo} 반환</li>
     *   <li>디렉토리: 하위 파일/디렉토리 수, 총 용량, 직접 자식 목록을 포함한 {@link FileInfo} 반환</li>
     *   <li>기타(소켓, 디바이스 등): 기본 정보만 포함한 {@link FileInfo} 반환</li>
     * </ul>
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

            String name = path.getFileName().toString();
            String absolutePath = path.toAbsolutePath().toString();
            String canonicalPath = canonicalOf(path);
            Path parentPath = path.getParent();
            String parent = (parentPath != null) ? parentPath.toString() : NO_PARENT;
            boolean isAbsolute = path.isAbsolute();
            boolean isHidden = isHiddenSafe(path);
            boolean isRegular = attrs.isRegularFile();
            boolean isDirectory = attrs.isDirectory();
            boolean isSymLink = Files.isSymbolicLink(path);
            boolean canRead = path.toFile().canRead();
            boolean canWrite = path.toFile().canWrite();
            boolean canExecute = path.toFile().canExecute();
            String createdAt = DT_FMT.format(attrs.creationTime().toInstant());
            String modifiedAt = DT_FMT.format(attrs.lastModifiedTime().toInstant());
            String accessAt = DT_FMT.format(attrs.lastAccessTime().toInstant());

            if (isRegular) {
                return new FileInfo(name, absolutePath, canonicalPath, parent,
                        isAbsolute, isHidden, isRegular, isDirectory, isSymLink,
                        canRead, canWrite, canExecute,
                        createdAt, modifiedAt, accessAt,
                        attrs.size(), null, null, null, null);
            }

            if (isDirectory) { // isRegular 블록을 통과했으므로 isRegular=false, isDirectory=true

                // 람다식 내에서 effectively final이 필요한데, 파일과 디렉토리 개수는 변경되어야 하므로 AtomicInteger 사용
                AtomicInteger fileCount = new AtomicInteger();
                AtomicInteger dirCount  = new AtomicInteger();
                AtomicLong    total     = new AtomicLong();

                Files.walkFileTree(path, new SimpleFileVisitor<>() {
                    /**
                     * 파일 방문 시 호출된다. 파일 수와 총 용량을 누적한다.
                     *
                     * @param file 방문한 파일 경로
                     * @param a    파일의 기본 속성
                     * @return 탐색을 계속하도록 {@link FileVisitResult#CONTINUE} 반환
                     */
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes a) {
                        fileCount.incrementAndGet();
                        total.addAndGet(a.size());
                        return FileVisitResult.CONTINUE;
                    }

                    /**
                     * 디렉토리 진입 전 호출된다. 루트 디렉토리를 제외한 하위 디렉토리 수를 누적한다.
                     *
                     * @param d 진입할 디렉토리 경로
                     * @param a 디렉토리의 기본 속성
                     * @return 탐색을 계속하도록 {@link FileVisitResult#CONTINUE} 반환
                     */
                    @Override
                    public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) {
                        if (!d.equals(path)) dirCount.incrementAndGet();
                        return FileVisitResult.CONTINUE;
                    }

                    /**
                     * 파일 방문 실패 시 호출된다. 권한 오류 등으로 접근 불가한 파일은 무시하고 탐색을 계속한다.
                     *
                     * @param file 방문에 실패한 파일 경로
                     * @param ex   발생한 예외
                     * @return 탐색을 계속하도록 {@link FileVisitResult#CONTINUE} 반환
                     */
                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException ex) {
                        return FileVisitResult.CONTINUE;
                    }
                });

                List<String> children = new ArrayList<>();
                try (var stream = Files.list(path)) {
                    stream.sorted().forEach(p -> {
                        String type = Files.isDirectory(p) ? DIR_PREFIX : FILE_PREFIX;
                        children.add(type + CHILDREN_INDENT + p.getFileName());
                    });
                }

                return new FileInfo(name, absolutePath, canonicalPath, parent,
                        isAbsolute, isHidden, isRegular, isDirectory, isSymLink,
                        canRead, canWrite, canExecute,
                        createdAt, modifiedAt, accessAt,
                        null, fileCount.get(), dirCount.get(), total.get(), children);
            }

            // 기타 (소켓, 디바이스 등) — isRegular=false, isDirectory=false
            return new FileInfo(name, absolutePath, canonicalPath, parent,
                    isAbsolute, isHidden, isRegular, isDirectory, isSymLink,
                    canRead, canWrite, canExecute,
                    createdAt, modifiedAt, accessAt,
                    null, null, null, null, null);

        } catch (IOException e) {
            throw new RuntimeException("파일 정보 읽기 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 주어진 경로의 정규 경로(canonical path)를 반환한다.
     * 심볼릭 링크가 있을 경우 실제 경로로 변환한다.
     *
     * @param path 정규 경로를 구할 경로
     * @return 정규 경로 문자열. 읽기 실패 시 오류 메시지 문자열 반환
     */
    private static String canonicalOf(Path path) {
        try {
            return path.toRealPath().toString();
        } catch (IOException e) {
            return CANONICAL_FAIL_PREFIX + e.getMessage() + ")";
        }
    }

    /**
     * 주어진 경로가 숨김 파일인지 안전하게 확인한다.
     * {@link IOException} 발생 시 숨김 파일이 아닌 것으로 간주한다.
     *
     * @param path 확인할 경로
     * @return 숨김 파일이면 {@code true}, 아니면 {@code false}
     */
    private static boolean isHiddenSafe(Path path) {
        try {
            return Files.isHidden(path);
        } catch (IOException e) {
            return false;
        }
    }
}