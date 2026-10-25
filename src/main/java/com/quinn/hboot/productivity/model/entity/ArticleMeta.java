package com.quinn.hboot.productivity.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_article_meta")
public class ArticleMeta {
    @TableId
    private Long articleId;
    private Long categoryId;
    private Long ownerId;
    private LocalDateTime updateTime;
}
