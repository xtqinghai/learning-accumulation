package com.quinn.hboot.knowledge.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.quinn.hboot.common.base.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_article_annotation")
public class KnowledgeAnnotation extends BaseEntity {
    private Long articleId;
    private Long articleVersionId;
    private Long authorId;
    private String sourceType;
    private String content;
    private String quotedText;
    private String qaSelectedText;
    private String anchorType;
    private Integer anchorStart;
    private Integer anchorEnd;
    private String anchorBlockId;
    private Long qaMessageId;
    private Integer pinned;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
