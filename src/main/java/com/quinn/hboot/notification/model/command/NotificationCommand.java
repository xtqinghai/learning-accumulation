package com.quinn.hboot.notification.model.command;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collection;

/** 业务模块创建站内信时使用的命令对象。 */
@Data
@Builder
public class NotificationCommand {

    private Collection<Long> recipientIds;
    private Long senderId;
    private String type;
    private String title;
    private String content;
    private String businessType;
    private String businessId;
    private String actionUrl;
    private LocalDateTime expireTime;
}
