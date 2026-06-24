package com.quinn.hboot.system.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface FileUploadSecurityService {

    void validateUpload(MultipartFile file);

    void validateChunk(MultipartFile file, String fileName);

    void validateMergedFile(File mergedFile, String fileName);

    void validateDeclaredFile(String fileName, Long fileSize);
}
