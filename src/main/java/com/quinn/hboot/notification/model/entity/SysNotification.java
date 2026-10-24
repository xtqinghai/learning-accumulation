package com.quinn.hboot.notification.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 用户站内信。每个接收人保存独立的已读状态。 */
@Data
@TableName("sys_notification")
public class SysNotification {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recipientId;
    private Long senderId;
    private String type;
    private String title;
    private String content;
    private String businessType;
    private String businessId;
    private String actionUrl;
    private Integer readStatus;
    private LocalDateTime readTime;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
}
