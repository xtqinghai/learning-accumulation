package com.quinn.hboot.photo.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.quinn.hboot.common.base.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("photo_media")
public class PhotoMedia extends BaseEntity {
    private Long spaceId;
    private Long uploaderId;
    private Long originalFileId;
    private String displayName;
    private String relativePath;
    private String mediaType;
    private String description;
    private Integer favorite;
    private Integer width;
    private Integer height;
    private Integer orientation;
    private LocalDateTime capturedAt;
    private String timeSource;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal altitude;
    private String locationName;
    private String cameraMake;
    private String cameraModel;
    private String lensModel;
    private Long durationMs;
    private String videoCodec;
    private String audioCodec;
    private Long bitRate;
    private BigDecimal frameRate;
    private String processStatus;
    private String processError;
    private String metadataJson;
    private Integer status;
    private LocalDateTime deletedAt;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
