package com.quinn.hboot.notification.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationVO {

    private Long id;
    private String type;
    private String title;
    private String content;
    private String businessType;
    private String businessId;
    private String actionUrl;
    private Integer readStatus;
    private LocalDateTime readTime;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
}
