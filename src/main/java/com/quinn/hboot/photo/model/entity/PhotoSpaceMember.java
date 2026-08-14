package com.quinn.hboot.photo.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.quinn.hboot.common.base.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("photo_space_member")
public class PhotoSpaceMember extends BaseEntity {
    private Long spaceId;
    private Long userId;
    private String role;
    private Integer status;
    private LocalDateTime joinedAt;
}
