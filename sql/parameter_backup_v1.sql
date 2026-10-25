CREATE TABLE IF NOT EXISTS `sys_system_parameter` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parameter_key` varchar(100) NOT NULL,
  `parameter_name` varchar(100) NOT NULL,
  `parameter_value` text NOT NULL,
  `category` varchar(50) NOT NULL,
  `sensitive` tinyint NOT NULL DEFAULT 0,
  `status` tinyint NOT NULL DEFAULT 1,
  `remark` varchar(500) NULL,
  `creator` varchar(100) NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(100) NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_system_parameter_key` (`parameter_key`),
  KEY `idx_sys_system_parameter_category_status` (`category`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统参数';

CREATE TABLE IF NOT EXISTS `sys_backup_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `backup_name` varchar(200) NOT NULL,
  `backup_type` varchar(30) NOT NULL,
  `status` tinyint NOT NULL,
  `verify_status` tinyint NOT NULL DEFAULT 0,
  `file_name` varchar(255) NOT NULL,
  `file_path` varchar(1000) NOT NULL,
  `manifest_path` varchar(1000) NULL,
  `checksum` varchar(128) NULL,
  `file_size` bigint NULL,
  `file_count` bigint NULL,
  `message` text NULL,
  `creator` varchar(100) NULL,
  `start_time` datetime NULL,
  `end_time` datetime NULL,
  `verify_time` datetime NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_backup_record_name` (`backup_name`),
  KEY `idx_sys_backup_record_status_time` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='备份记录';

INSERT INTO `sys_system_parameter`
  (`parameter_key`, `parameter_name`, `parameter_value`, `category`, `sensitive`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`)
SELECT 'app.site.name', '系统名称', 'H-Boot', '应用', 0, 1, '示例系统参数，可在管理页面修改。', 'system', NOW(), 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_system_parameter` WHERE `parameter_key` = 'app.site.name');

INSERT INTO `sys_menu`
  (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT 2, '参数配置', '/system/parameter', 'system/parameter/index', 1, 'system:parameter:query', 'SetUp', 12, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `path` = '/system/parameter');

INSERT INTO `sys_menu`
  (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT 2, '备份恢复', '/system/backup', 'system/backup/index', 1, 'system:backup:query', 'FolderChecked', 13, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `path` = '/system/backup');

INSERT INTO `sys_menu`
  (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT m.id, v.name, NULL, NULL, 2, v.permission, NULL, v.sort, 1, NOW(), NOW()
FROM `sys_menu` m
JOIN (
  SELECT '新增参数' AS name, 'system:parameter:add' AS permission, 1 AS sort
  UNION ALL SELECT '编辑参数', 'system:parameter:edit', 2
  UNION ALL SELECT '删除参数', 'system:parameter:delete', 3
) v
WHERE m.path = '/system/parameter'
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` x WHERE x.permission = v.permission);

INSERT INTO `sys_menu`
  (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT m.id, v.name, NULL, NULL, 2, v.permission, NULL, v.sort, 1, NOW(), NOW()
FROM `sys_menu` m
JOIN (
  SELECT '创建备份' AS name, 'system:backup:create' AS permission, 1 AS sort
  UNION ALL SELECT '校验备份', 'system:backup:verify', 2
  UNION ALL SELECT '下载备份', 'system:backup:download', 3
  UNION ALL SELECT '恢复数据库', 'system:backup:restore', 4
) v
WHERE m.path = '/system/backup'
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` x WHERE x.permission = v.permission);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, m.id
FROM `sys_menu` m
WHERE (m.path IN ('/system/parameter', '/system/backup') OR m.permission IN (
  'system:parameter:add', 'system:parameter:edit', 'system:parameter:delete',
  'system:backup:create', 'system:backup:verify', 'system:backup:download', 'system:backup:restore'
))
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id = 1 AND rm.menu_id = m.id
  );
