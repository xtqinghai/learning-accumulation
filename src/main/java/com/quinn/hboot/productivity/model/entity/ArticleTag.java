package com.quinn.hboot.productivity.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("kb_article_tag")
public class ArticleTag {
    private Long articleId;
    private Long tagId;
    private Long ownerId;
}
