package com.quinn.hboot.knowledge.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.quinn.hboot.common.base.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_source_file")
public class KnowledgeSourceFile extends BaseEntity {
    private Long spaceId;
    private Long ownerId;
    private Long sysFileId;
    private Long articleId;
    private String fileName;
    private String fileExt;
    private String contentType;
    private Long fileSize;
    private String parsedText;
    private String status;
    private String errorMessage;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
