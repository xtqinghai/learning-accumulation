package com.quinn.hboot.knowledge.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.quinn.hboot.common.base.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_article_version")
public class KnowledgeArticleVersion extends BaseEntity {
    private Long articleId;
    private Integer versionNo;
    private String title;
    private String keywords;
    private String summary;
    private String content;
    private String contentHash;
    private String source;
    private Long createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
