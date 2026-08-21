package com.quinn.hboot.knowledge.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.quinn.hboot.common.base.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_import_task")
public class KnowledgeImportTask extends BaseEntity {
    private Long sourceFileId;
    private Long ownerId;
    private Long articleId;
    private String status;
    private Integer progress;
    private String currentStage;
    private Integer retryCount;
    private String errorMessage;
    private String model;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
