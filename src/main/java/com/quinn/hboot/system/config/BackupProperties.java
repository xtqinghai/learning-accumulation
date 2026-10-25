package com.quinn.hboot.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/** Operational settings for database backup commands. Values are deployment-only, never editable through the UI. */
@Data
@Component
@ConfigurationProperties(prefix = "backup")
public class BackupProperties {

    private String directory;
    private String pgDumpCommand = "pg_dump";
    private String pgRestoreCommand = "pg_restore";
    private Duration processTimeout = Duration.ofMinutes(10);
    private boolean restoreEnabled;
}
