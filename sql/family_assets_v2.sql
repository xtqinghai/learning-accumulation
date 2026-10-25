-- P2 家庭数字资产（在 photo_album_v1.sql 后执行）
CREATE TABLE IF NOT EXISTS `family_document` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `space_id` bigint NOT NULL,
  `owner_id` bigint NOT NULL,
  `file_id` bigint NOT NULL,
  `title` varchar(255) NOT NULL,
  `category` varchar(40) NOT NULL DEFAULT 'OTHER' COMMENT 'PDF/OFFICE/OTHER',
  `tags` varchar(1000) NULL,
  `extracted_text` longtext NULL,
  `version_no` int NOT NULL DEFAULT 1,
  `status` varchar(20) NOT NULL DEFAULT 'READY',
  `error_message` varchar(2000) NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`), INDEX `idx_family_document_search` (`space_id`, `category`, `status`, `deleted`, `update_time`), INDEX `idx_family_document_owner` (`owner_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='家庭文档库';
CREATE TABLE IF NOT EXISTS `family_document_version` (
  `id` bigint NOT NULL AUTO_INCREMENT, `document_id` bigint NOT NULL, `file_id` bigint NOT NULL, `version_no` int NOT NULL, `change_note` varchar(500) NULL, `created_by` bigint NOT NULL, `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`), UNIQUE INDEX `uk_family_document_version` (`document_id`, `version_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='家庭文档版本';
CREATE TABLE IF NOT EXISTS `photo_duplicate_candidate` (
  `id` bigint NOT NULL AUTO_INCREMENT, `space_id` bigint NOT NULL, `media_id` bigint NOT NULL, `duplicate_media_id` bigint NOT NULL, `match_type` varchar(20) NOT NULL COMMENT 'EXACT/SIMILAR', `confidence` decimal(6,5) NOT NULL DEFAULT 1, `marked` tinyint NOT NULL DEFAULT 1, `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`), UNIQUE INDEX `uk_photo_duplicate_pair` (`media_id`, `duplicate_media_id`), INDEX `idx_photo_duplicate_space` (`space_id`, `marked`, `match_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='重复媒体候选标记';
CREATE TABLE IF NOT EXISTS `family_share_link` (
  `id` bigint NOT NULL AUTO_INCREMENT, `space_id` bigint NOT NULL, `created_by` bigint NOT NULL, `resource_type` varchar(30) NOT NULL, `resource_id` bigint NULL, `token` varchar(80) NOT NULL, `expires_at` datetime NULL, `status` tinyint NOT NULL DEFAULT 1, `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`), UNIQUE INDEX `uk_family_share_token` (`token`), INDEX `idx_family_share_space` (`space_id`, `status`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='受控分享链接';
CREATE TABLE IF NOT EXISTS `family_device` (
  `id` bigint NOT NULL AUTO_INCREMENT, `space_id` bigint NOT NULL, `user_id` bigint NOT NULL, `device_name` varchar(100) NOT NULL, `device_type` varchar(30) NOT NULL, `device_key` varchar(120) NOT NULL, `sync_cursor` varchar(120) NULL, `last_seen_at` datetime NULL, `status` tinyint NOT NULL DEFAULT 1, `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP, `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`), UNIQUE INDEX `uk_family_device_key` (`device_key`), INDEX `idx_family_device_space_user` (`space_id`, `user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='家庭设备登记';
CREATE TABLE IF NOT EXISTS `family_sync_log` (
  `id` bigint NOT NULL AUTO_INCREMENT, `device_id` bigint NOT NULL, `direction` varchar(20) NOT NULL, `entity_type` varchar(40) NOT NULL, `entity_id` bigint NULL, `operation` varchar(20) NOT NULL, `conflict_status` varchar(20) NOT NULL DEFAULT 'NONE', `message` varchar(2000) NULL, `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`), INDEX `idx_family_sync_device_time` (`device_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='家庭设备同步日志';

INSERT INTO `sys_menu` (`parent_id`,`name`,`path`,`component`,`type`,`permission`,`icon`,`sort`,`status`,`create_time`,`update_time`)
SELECT 0,'家庭资产','/family-assets','family-assets/index',1,'family:asset:list','FolderOpened',5,1,NOW(),NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `path`='/family-assets');
INSERT INTO `sys_role_menu` (`role_id`,`menu_id`) SELECT 1,m.id FROM `sys_menu` m WHERE m.path='/family-assets' AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id=1 AND rm.menu_id=m.id);
