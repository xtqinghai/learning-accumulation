package com.quinn.hboot.photo.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.quinn.hboot.common.base.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("photo_media_tag")
public class PhotoMediaTag extends BaseEntity {
    private Long mediaId;
    private Long tagId;
    private Long createdBy;
    private LocalDateTime createTime;
}
