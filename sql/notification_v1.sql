-- 站内信：一位接收人对应一条记录，已读状态互不影响。
CREATE TABLE IF NOT EXISTS `sys_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `recipient_id` bigint NOT NULL,
  `sender_id` bigint NULL,
  `type` varchar(32) NOT NULL DEFAULT 'SYSTEM',
  `title` varchar(255) NOT NULL,
  `content` varchar(5000) NOT NULL,
  `business_type` varchar(64) NULL,
  `business_id` varchar(100) NULL,
  `action_url` varchar(500) NULL,
  `read_status` tinyint NOT NULL DEFAULT 0,
  `read_time` datetime NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expire_time` datetime NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_sys_notification_recipient_unread` (`recipient_id`, `status`, `read_status`, `create_time`),
  INDEX `idx_sys_notification_recipient_page` (`recipient_id`, `status`, `create_time`),
  INDEX `idx_sys_notification_business` (`business_type`, `business_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户站内信';

INSERT INTO `sys_menu`
  (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT 0, '我的消息', '/notification', 'notification/index', 1, NULL, 'Bell', 7, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `path` = '/notification');

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.id, m.id
FROM `sys_role` r
CROSS JOIN `sys_menu` m
WHERE r.status = 1
  AND m.path = '/notification'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id = r.id AND rm.menu_id = m.id
  );
