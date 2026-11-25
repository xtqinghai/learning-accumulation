package com.quinn.hboot.knowledge.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_article_chunk")
public class KnowledgeArticleChunk {
    private Long id;
    private Long articleId;
    private Long sourceFileId;
    private Long spaceId;
    private Integer chunkNo;
    private String content;
    private String searchContent;
    private Integer charStart;
    private Integer charEnd;
    @TableField(exist = false)
    private Double similarity;
    @TableField(exist = false)
    private String embeddingText;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

