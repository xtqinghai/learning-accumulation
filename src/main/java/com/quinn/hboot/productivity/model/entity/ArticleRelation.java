package com.quinn.hboot.productivity.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_article_relation")
public class ArticleRelation {
    private Long articleId;
    private Long relatedArticleId;
    private Long ownerId;
    private LocalDateTime createTime;
}
