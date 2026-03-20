package dev.wonyoung.domain;

public record FileInfo(
        String name,
        String path,
        String absolutePath,
        String canonicalPath,
        String parent,
        boolean absolute,
        boolean hidden,
        boolean regularFile,
        boolean directory,
        boolean canRead,
        boolean canWrite,
        boolean canExecute,
        String lastModifiedAt,
        Long size,           // 파일 전용 (디렉토리이면 null)
        Long wordCount,      // txt 파일 전용 (다른 경우 null)
        Integer fileCount,   // 디렉토리 전용 (파일이면 null)
        Integer dirCount,
        Long totalSize
) {}