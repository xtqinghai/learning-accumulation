package com.quinn.hboot.system.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/** 本地磁盘文件存储。所有路径都限制在配置的数据目录内。 */
@Component
@ConditionalOnProperty(name = "file.storage", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageAdapter implements FileStorageAdapter {

    private final Path basePath;

    public LocalFileStorageAdapter(@Value("${file.local.base-path}") String basePath) {
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize();
    }

    @Override
    public String save(InputStream inputStream, String path, String contentType) throws IOException {
        Path target = resolve(path);
        Files.createDirectories(target.getParent());
        Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        return normalizePath(path);
    }

    @Override
    public InputStream get(String path) throws IOException {
        Path target = resolve(path);
        if (!Files.isRegularFile(target)) {
            throw new IOException("文件不存在: " + normalizePath(path));
        }
        return Files.newInputStream(target);
    }

    @Override
    public void delete(String path) throws IOException {
        Files.deleteIfExists(resolve(path));
    }

    @Override
    public long size(String path) throws IOException {
        return Files.size(resolve(path));
    }

    @Override
    public Path resolve(String path) {
        String normalizedPath = normalizePath(path);
        Path target = basePath.resolve(normalizedPath).normalize();
        if (!target.startsWith(basePath)) {
            throw new IllegalArgumentException("非法文件路径");
        }
        return target;
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        String normalized = path.replace('\\', '/');
        if (normalized.startsWith("/") || Paths.get(normalized).isAbsolute()) {
            throw new IllegalArgumentException("非法文件路径");
        }
        return normalized;
    }
}
