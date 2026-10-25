package com.quinn.hboot.system.backup.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BackupRecordVO {

    private Long id;
    private String backupName;
    private String backupType;
    private Integer status;
    private Integer verifyStatus;
    private String fileName;
    private String checksum;
    private Long fileSize;
    private Long fileCount;
    private String message;
    private String creator;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime verifyTime;
    private LocalDateTime createTime;
}
