package com.quinn.hboot.photo.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.quinn.hboot.common.base.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("photo_tag")
public class PhotoTag extends BaseEntity {
    private Long spaceId;
    private String name;
    private String normalizedName;
    private String color;
    private Integer usageCount;
    private Long createdBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
