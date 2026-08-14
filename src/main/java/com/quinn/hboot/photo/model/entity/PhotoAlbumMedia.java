package com.quinn.hboot.photo.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.quinn.hboot.common.base.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("photo_album_media")
public class PhotoAlbumMedia extends BaseEntity {
    private Long albumId;
    private Long mediaId;
    private Integer sortOrder;
    private Long addedBy;
    private LocalDateTime addedAt;
}
