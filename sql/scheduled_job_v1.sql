-- 内置 Quartz 调度的业务配置与执行日志。
CREATE TABLE IF NOT EXISTS `sys_scheduled_job` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_name` varchar(100) NOT NULL,
  `handler_key` varchar(100) NOT NULL,
  `cron_expression` varchar(120) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `remark` varchar(500) NULL,
  `creator` varchar(100) NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(100) NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_scheduled_job_name` (`job_name`),
  KEY `idx_sys_scheduled_job_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='定时任务配置';

CREATE TABLE IF NOT EXISTS `sys_scheduled_job_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_id` bigint NOT NULL,
  `job_name` varchar(100) NOT NULL,
  `handler_key` varchar(100) NOT NULL,
  `trigger_type` varchar(20) NOT NULL,
  `status` tinyint NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NULL,
  `duration_ms` bigint NULL,
  `message` text NULL,
  `error_stack` text NULL,
  PRIMARY KEY (`id`),
  KEY `idx_sys_scheduled_job_log_job_time` (`job_id`, `start_time`),
  KEY `idx_sys_scheduled_job_log_status_time` (`status`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='定时任务执行日志';

-- 默认保持停用，管理员可通过“立即执行”安全验证后再决定是否启用。
INSERT INTO `sys_scheduled_job`
  (`job_name`, `handler_key`, `cron_expression`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`)
SELECT '清理过期站内信', 'notificationExpirationCleanup', '0 0 3 * * ?', 0,
       '仅删除 expire_time 已过期的站内信；默认停止，按需启用。', 'system', NOW(), 'system', NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_scheduled_job` WHERE `job_name` = '清理过期站内信'
);

INSERT INTO `sys_menu`
  (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT 2, '定时任务', '/system/scheduled-job', 'system/scheduled-job/index', 1, 'system:job:query', 'Timer', 11, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `path` = '/system/scheduled-job');

INSERT INTO `sys_menu`
  (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT m.id, v.name, NULL, NULL, 2, v.permission, NULL, v.sort, 1, NOW(), NOW()
FROM `sys_menu` m
JOIN (
  SELECT '新增任务' AS name, 'system:job:add' AS permission, 1 AS sort
  UNION ALL SELECT '编辑任务', 'system:job:edit', 2
  UNION ALL SELECT '删除任务', 'system:job:delete', 3
  UNION ALL SELECT '执行任务', 'system:job:run', 4
) v
WHERE m.path = '/system/scheduled-job'
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` x WHERE x.permission = v.permission);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, m.id
FROM `sys_menu` m
WHERE (m.path = '/system/scheduled-job' OR m.permission IN (
  'system:job:add', 'system:job:edit', 'system:job:delete', 'system:job:run'
))
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id = 1 AND rm.menu_id = m.id
  );
