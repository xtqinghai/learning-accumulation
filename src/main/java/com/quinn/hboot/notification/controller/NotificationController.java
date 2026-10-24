package com.quinn.hboot.notification.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quinn.hboot.common.R;
import com.quinn.hboot.common.log.SysLog;
import com.quinn.hboot.notification.model.query.NotificationQuery;
import com.quinn.hboot.notification.model.vo.NotificationUnreadVO;
import com.quinn.hboot.notification.model.vo.NotificationVO;
import com.quinn.hboot.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "我的消息接口")
@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "我的消息分页列表")
    @GetMapping("/page")
    public R<IPage<NotificationVO>> page(NotificationQuery query) {
        return R.ok(notificationService.getMyPage(query));
    }

    @Operation(summary = "未读消息数量")
    @GetMapping("/unread-count")
    public R<NotificationUnreadVO> unreadCount() {
        return R.ok(new NotificationUnreadVO(notificationService.getMyUnreadCount()));
    }

    @Operation(summary = "标记消息已读")
    @PutMapping("/{id}/read")
    @SysLog(module = "我的消息", type = "标记已读")
    public R<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return R.ok();
    }

    @Operation(summary = "全部标记已读")
    @PutMapping("/read-all")
    @SysLog(module = "我的消息", type = "全部标记已读")
    public R<Void> markAllRead() {
        notificationService.markAllRead();
        return R.ok();
    }

    @Operation(summary = "删除我的消息")
    @DeleteMapping("/{id}")
    @SysLog(module = "我的消息", type = "删除")
    public R<Void> delete(@PathVariable Long id) {
        notificationService.deleteMyNotification(id);
        return R.ok();
    }
}
