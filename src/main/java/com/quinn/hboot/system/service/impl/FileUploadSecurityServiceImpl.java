package com.quinn.hboot.system.service.impl;

import com.quinn.hboot.common.exception.BusinessException;
import com.quinn.hboot.config.properties.FileSecurityProperties;
import com.quinn.hboot.system.service.FileAntivirusService;
import com.quinn.hboot.system.service.FileUploadSecurityService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileUploadSecurityServiceImpl implements FileUploadSecurityService {

    private static final Set<String> DEFAULT_BLOCKED_EXTENSIONS = Set.of(
        "exe", "bat", "cmd", "com", "msi", "sh", "ps1", "vbs", "js", "jse", "jar", "war",
        "php", "jsp", "jspx", "asp", "aspx", "html", "htm"
    );

    private final FileSecurityProperties fileSecurityProperties;
    private final ObjectProvider<FileAntivirusService> fileAntivirusServiceProvider;

    @Override
    public void validateUpload(MultipartFile file) {
        if (!fileSecurityProperties.isEnabled()) {
            return;
        }
        validateFileNameAndSize(file.getOriginalFilename(), file.getSize());
        validateExtension(file.getOriginalFilename());
        validateMimeType(file.getContentType());
        if (fileSecurityProperties.isAntivirusEnabled()) {
            getFileAntivirusService().scan(file);
        }
    }

    @Override
    public void validateChunk(MultipartFile file, String fileName) {
        if (!fileSecurityProperties.isEnabled()) {
            return;
        }
        validateFileNameAndSize(fileName, file.getSize());
        validateExtension(fileName);
    }

    @Override
    public void validateMergedFile(File mergedFile, String fileName) {
        if (!fileSecurityProperties.isEnabled()) {
            return;
        }
        validateFileNameAndSize(fileName, mergedFile.length());
        validateExtension(fileName);
        if (fileSecurityProperties.isAntivirusEnabled()) {
            getFileAntivirusService().scan(mergedFile, fileName);
        }
    }

    @Override
    public void validateDeclaredFile(String fileName, Long fileSize) {
        if (!fileSecurityProperties.isEnabled()) {
            return;
        }
        validateFileNameAndSize(fileName, fileSize == null ? 0L : fileSize);
        validateExtension(fileName);
    }

    private void validateFileNameAndSize(String fileName, long fileSize) {
        if (fileName == null || fileName.isBlank()) {
            throw new BusinessException("上传文件名不能为空");
        }
        if (fileSize <= 0) {
            throw new BusinessException("上传文件不能为空");
        }
        if (fileSecurityProperties.getMaxFileSize() > 0 && fileSize > fileSecurityProperties.getMaxFileSize()) {
            throw new BusinessException("上传文件大小超出限制");
        }
    }

    private void validateExtension(String fileName) {
        String extension = normalizeExtension(FilenameUtils.getExtension(fileName));
        if (extension.isBlank()) {
            throw new BusinessException("上传文件缺少扩展名");
        }

        Set<String> blockedExtensions = normalizeToSet(fileSecurityProperties.getBlockedExtensions());
        if (DEFAULT_BLOCKED_EXTENSIONS.contains(extension) || blockedExtensions.contains(extension)) {
            throw new BusinessException("上传文件类型存在安全风险，已被禁止");
        }

        Set<String> allowedExtensions = normalizeToSet(fileSecurityProperties.getAllowedExtensions());
        if (!allowedExtensions.isEmpty() && !allowedExtensions.contains(extension)) {
            throw new BusinessException("上传文件扩展名不在允许范围内");
        }
    }

    private void validateMimeType(String contentType) {
        Set<String> allowedMimeTypes = normalizeToSet(fileSecurityProperties.getAllowedMimeTypes());
        if (allowedMimeTypes.isEmpty()) {
            return;
        }
        String normalized = normalizeMimeType(contentType);
        if (normalized.isBlank() || !allowedMimeTypes.contains(normalized)) {
            throw new BusinessException("上传文件 MIME 类型不在允许范围内");
        }
    }

    private Set<String> normalizeToSet(List<String> source) {
        return source == null ? Set.of() : source.stream()
            .map(this::normalizeExtension)
            .filter(item -> !item.isBlank())
            .collect(Collectors.toSet());
    }

    private String normalizeExtension(String extension) {
        if (extension == null) {
            return "";
        }
        return extension.trim().toLowerCase(Locale.ROOT).replace(".", "");
    }

    private String normalizeMimeType(String mimeType) {
        if (mimeType == null) {
            return "";
        }
        return mimeType.trim().toLowerCase(Locale.ROOT);
    }

    private FileAntivirusService getFileAntivirusService() {
        return fileAntivirusServiceProvider.getIfAvailable(NoopAntivirusService::new);
    }

    private static final class NoopAntivirusService implements FileAntivirusService {

        @Override
        public void scan(MultipartFile file) {
        }

        @Override
        public void scan(File file, String originalFileName) {
        }
    }
}
