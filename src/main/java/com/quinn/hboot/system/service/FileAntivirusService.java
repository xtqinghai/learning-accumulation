package com.quinn.hboot.system.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface FileAntivirusService {

    /**
     * Antivirus scan hook. Default implementation can be a no-op.
     */
    void scan(MultipartFile file);

    /**
     * Antivirus scan hook for merged temp files.
     */
    void scan(File file, String originalFileName);
}
