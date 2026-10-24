package com.quinn.hboot.notification.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quinn.hboot.notification.model.command.NotificationCommand;
import com.quinn.hboot.notification.model.query.NotificationQuery;
import com.quinn.hboot.notification.model.vo.NotificationVO;

public interface NotificationService {

    void send(NotificationCommand command);

    IPage<NotificationVO> getMyPage(NotificationQuery query);

    long getMyUnreadCount();

    void markRead(Long id);

    void markAllRead();

    void deleteMyNotification(Long id);
}
