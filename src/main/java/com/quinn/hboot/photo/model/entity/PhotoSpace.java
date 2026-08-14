package com.quinn.hboot.photo.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.quinn.hboot.common.base.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("photo_space")
public class PhotoSpace extends BaseEntity {
    private String name;
    private String type;
    private Long ownerId;
    private Long coverMediaId;
    private Long quotaBytes;
    private Long usedBytes;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
