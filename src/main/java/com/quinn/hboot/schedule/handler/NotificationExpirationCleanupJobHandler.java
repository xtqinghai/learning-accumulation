package com.quinn.hboot.schedule.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quinn.hboot.notification.mapper.NotificationMapper;
import com.quinn.hboot.notification.model.entity.SysNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Deletes only messages whose explicit expiry time has passed. */
@Component
@RequiredArgsConstructor
public class NotificationExpirationCleanupJobHandler implements ScheduledJobHandler {

    private final NotificationMapper notificationMapper;

    @Override
    public String key() {
        return "notificationExpirationCleanup";
    }

    @Override
    public String name() {
        return "清理过期站内信";
    }

    @Override
    public String execute() {
        int deleted = notificationMapper.delete(new LambdaQueryWrapper<SysNotification>()
            .isNotNull(SysNotification::getExpireTime)
            .lt(SysNotification::getExpireTime, LocalDateTime.now()));
        return "已清理 " + deleted + " 条过期站内信";
    }
}
