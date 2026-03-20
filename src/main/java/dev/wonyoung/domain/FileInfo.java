package dev.wonyoung.domain;

import java.util.List;

public record FileInfo(
        String name,
        String absolutePath,
        String canonicalPath,
        String parent,
        boolean absolute,
        boolean hidden,
        boolean regularFile,
        boolean directory,
        boolean symbolicLink,
        boolean canRead,
        boolean canWrite,
        boolean canExecute,
        String createdAt,
        String lastModifiedAt,
        String lastAccessAt,
        Long size,           // 파일 전용 (디렉토리이면 null)
        Integer fileCount,   // 디렉토리 전용 (파일이면 null)
        Integer dirCount,
        Long totalSize,
        List<String> children
) {}