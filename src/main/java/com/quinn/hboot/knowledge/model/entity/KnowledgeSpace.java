package com.quinn.hboot.knowledge.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.quinn.hboot.common.base.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_space")
public class KnowledgeSpace extends BaseEntity {
    private String name;
    private Long ownerId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
