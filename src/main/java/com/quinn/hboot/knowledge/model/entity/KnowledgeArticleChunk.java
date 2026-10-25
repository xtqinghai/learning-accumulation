package com.quinn.hboot.knowledge.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_article_chunk")
public class KnowledgeArticleChunk {
    private Long id;
    private Long articleId;
    private Long spaceId;
    private Integer chunkNo;
    private String content;
    private String searchContent;
    private Integer charStart;
    private Integer charEnd;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

