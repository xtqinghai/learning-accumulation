package com.quinn.hboot.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.common.exception.BusinessException;
import com.quinn.hboot.notification.mapper.NotificationMapper;
import com.quinn.hboot.notification.model.command.NotificationCommand;
import com.quinn.hboot.notification.model.entity.SysNotification;
import com.quinn.hboot.notification.model.query.NotificationQuery;
import com.quinn.hboot.notification.model.vo.NotificationVO;
import com.quinn.hboot.notification.service.NotificationService;
import com.quinn.hboot.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final int ACTIVE = 1;
    private static final int INACTIVE = 0;
    private static final int UNREAD = 0;
    private static final int READ = 1;

    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void send(NotificationCommand command) {
        if (command == null || command.getRecipientIds() == null || command.getRecipientIds().isEmpty()) {
            return;
        }
        if (!StringUtils.hasText(command.getTitle()) || !StringUtils.hasText(command.getContent())) {
            throw new BusinessException("消息标题和内容不能为空");
        }
        if (command.getTitle().length() > 255 || command.getContent().length() > 5000) {
            throw new BusinessException("消息标题或内容长度超出限制");
        }

        Set<Long> recipients = uniqueRecipientIds(command.getRecipientIds());
        if (recipients.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (Long recipientId : recipients) {
            SysNotification notification = new SysNotification();
            notification.setRecipientId(recipientId);
            notification.setSenderId(command.getSenderId());
            notification.setType(defaultValue(command.getType(), "SYSTEM"));
            notification.setTitle(command.getTitle());
            notification.setContent(command.getContent());
            notification.setBusinessType(command.getBusinessType());
            notification.setBusinessId(command.getBusinessId());
            notification.setActionUrl(command.getActionUrl());
            notification.setReadStatus(UNREAD);
            notification.setStatus(ACTIVE);
            notification.setCreateTime(now);
            notification.setExpireTime(command.getExpireTime());
            notificationMapper.insert(notification);
        }
    }

    @Override
    public IPage<NotificationVO> getMyPage(NotificationQuery query) {
        if (query == null) {
            query = new NotificationQuery();
        }
        return notificationMapper.selectRecipientPage(query.buildPage(), requireCurrentUserId(), query);
    }

    @Override
    public long getMyUnreadCount() {
        LocalDateTime now = LocalDateTime.now();
        return notificationMapper.selectCount(new LambdaQueryWrapper<SysNotification>()
            .eq(SysNotification::getRecipientId, requireCurrentUserId())
            .eq(SysNotification::getStatus, ACTIVE)
            .eq(SysNotification::getReadStatus, UNREAD)
            .and(wrapper -> wrapper.isNull(SysNotification::getExpireTime)
                .or().gt(SysNotification::getExpireTime, now)));
    }

    @Override
    public void markRead(Long id) {
        if (id == null) {
            throw new BusinessException("消息 ID 不能为空");
        }
        int updated = notificationMapper.update(null, new LambdaUpdateWrapper<SysNotification>()
            .eq(SysNotification::getId, id)
            .eq(SysNotification::getRecipientId, requireCurrentUserId())
            .eq(SysNotification::getStatus, ACTIVE)
            .eq(SysNotification::getReadStatus, UNREAD)
            .set(SysNotification::getReadStatus, READ)
            .set(SysNotification::getReadTime, LocalDateTime.now()));
        if (updated == 0) {
            SysNotification notification = notificationMapper.selectById(id);
            if (notification == null || !requireCurrentUserId().equals(notification.getRecipientId())) {
                throw new BusinessException("消息不存在或无权操作");
            }
        }
    }

    @Override
    public void markAllRead() {
        notificationMapper.update(null, new LambdaUpdateWrapper<SysNotification>()
            .eq(SysNotification::getRecipientId, requireCurrentUserId())
            .eq(SysNotification::getStatus, ACTIVE)
            .eq(SysNotification::getReadStatus, UNREAD)
            .set(SysNotification::getReadStatus, READ)
            .set(SysNotification::getReadTime, LocalDateTime.now()));
    }

    @Override
    public void deleteMyNotification(Long id) {
        if (id == null) {
            throw new BusinessException("消息 ID 不能为空");
        }
        int updated = notificationMapper.update(null, new LambdaUpdateWrapper<SysNotification>()
            .eq(SysNotification::getId, id)
            .eq(SysNotification::getRecipientId, requireCurrentUserId())
            .eq(SysNotification::getStatus, ACTIVE)
            .set(SysNotification::getStatus, INACTIVE));
        if (updated == 0) {
            throw new BusinessException("消息不存在或无权操作");
        }
    }

    private Long requireCurrentUserId() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new BusinessException("未获取到当前登录用户");
        }
        return userId;
    }

    private Set<Long> uniqueRecipientIds(Collection<Long> recipientIds) {
        Set<Long> result = new LinkedHashSet<>();
        recipientIds.stream().filter(id -> id != null && id > 0).forEach(result::add);
        return result;
    }

    private String defaultValue(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
