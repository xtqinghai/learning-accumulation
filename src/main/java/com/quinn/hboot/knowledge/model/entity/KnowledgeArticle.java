package com.quinn.hboot.knowledge.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.quinn.hboot.common.base.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_article")
public class KnowledgeArticle extends BaseEntity {
    private Long spaceId;
    private Long ownerId;
    private String title;
    private String keywords;
    private String summary;
    private String content;
    private String searchContent;
    private String status;
    private String sourceType;
    private Long sourceFileId;
    private Long currentVersionId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
    @TableField(exist = false)
    private Double relevance;
    @TableField(exist = false)
    private Long annotationCount;
}
