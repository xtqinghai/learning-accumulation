package com.quinn.hboot.photo.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.quinn.hboot.common.base.BaseEntity;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("photo_album")
public class PhotoAlbum extends BaseEntity {
    private Long spaceId;
    private String name;
    private String description;
    private Long coverMediaId;
    private LocalDate albumDate;
    private String locationName;
    private Integer sortOrder;
    private Integer mediaCount;
    private Long totalSize;
    private Long createdBy;
    private Integer status;
    private LocalDateTime deletedAt;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
