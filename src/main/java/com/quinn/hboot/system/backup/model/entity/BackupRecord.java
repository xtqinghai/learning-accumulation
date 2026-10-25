package com.quinn.hboot.system.backup.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_backup_record")
public class BackupRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String backupName;
    private String backupType;
    private Integer status;
    private Integer verifyStatus;
    private String fileName;
    private String filePath;
    private String manifestPath;
    private String checksum;
    private Long fileSize;
    private Long fileCount;
    private String message;
    private String creator;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime verifyTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
