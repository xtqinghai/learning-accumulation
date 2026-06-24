package com.quinn.hboot.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "file.security")
public class FileSecurityProperties {

    /**
     * Whether upload security validation is enabled.
     */
    private boolean enabled = true;

    /**
     * Max single file size in bytes. Default 100 MB.
     */
    private long maxFileSize = 104857600L;

    /**
     * Allowed file extensions, lower-case and without dot.
     */
    private List<String> allowedExtensions = new ArrayList<>();

    /**
     * Allowed MIME content types.
     */
    private List<String> allowedMimeTypes = new ArrayList<>();

    /**
     * Dangerous extensions blocked even if accidentally added to allow-list.
     */
    private List<String> blockedExtensions = new ArrayList<>();

    /**
     * Whether to enable antivirus hook.
     */
    private boolean antivirusEnabled = false;
}
