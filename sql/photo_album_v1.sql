-- 家庭相册 P0 数据库增量脚本
-- 适用: MySQL 8.0 / h_boot

ALTER TABLE `sys_file`
  ADD COLUMN `owner_id` bigint NULL COMMENT '文件所有者用户ID' AFTER `id`,
  ADD COLUMN `biz_type` varchar(40) NOT NULL DEFAULT 'GENERAL' COMMENT '业务类型' AFTER `owner_id`,
  ADD COLUMN `hash_algorithm` varchar(20) NOT NULL DEFAULT 'MD5' COMMENT '哈希算法' AFTER `file_hash`,
  ADD COLUMN `storage_status` varchar(20) NOT NULL DEFAULT 'READY' COMMENT '存储状态' AFTER `storage_type`;

ALTER TABLE `sys_file`
  DROP INDEX `uk_hash`,
  ADD UNIQUE INDEX `uk_owner_biz_hash` (`owner_id`, `biz_type`, `file_hash`),
  ADD INDEX `idx_sys_file_owner_status` (`owner_id`, `status`);

CREATE TABLE IF NOT EXISTS `photo_space` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '空间名称',
  `type` varchar(20) NOT NULL DEFAULT 'PERSONAL' COMMENT 'PERSONAL/FAMILY',
  `owner_id` bigint NOT NULL COMMENT '空间所有者',
  `cover_media_id` bigint NULL COMMENT '封面媒体ID',
  `quota_bytes` bigint NULL COMMENT '容量上限',
  `used_bytes` bigint NOT NULL DEFAULT 0 COMMENT '已用容量缓存',
  `status` tinyint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_photo_space_owner_type` (`owner_id`, `type`),
  INDEX `idx_photo_space_owner` (`owner_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='相册空间';

CREATE TABLE IF NOT EXISTS `photo_space_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `space_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `role` varchar(20) NOT NULL COMMENT 'OWNER/MANAGER/CONTRIBUTOR/VIEWER',
  `status` tinyint NOT NULL DEFAULT 1,
  `joined_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_photo_space_member` (`space_id`, `user_id`),
  INDEX `idx_photo_member_user` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='相册空间成员';

CREATE TABLE IF NOT EXISTS `photo_album` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `space_id` bigint NOT NULL,
  `name` varchar(200) NOT NULL,
  `description` varchar(1000) NULL,
  `cover_media_id` bigint NULL,
  `album_date` date NULL,
  `location_name` varchar(255) NULL,
  `sort_order` int NOT NULL DEFAULT 0,
  `media_count` int NOT NULL DEFAULT 0,
  `total_size` bigint NOT NULL DEFAULT 0,
  `created_by` bigint NOT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `deleted_at` datetime NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_photo_album_space` (`space_id`, `status`, `sort_order`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='相册';

CREATE TABLE IF NOT EXISTS `photo_media` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `space_id` bigint NOT NULL,
  `uploader_id` bigint NOT NULL,
  `original_file_id` bigint NOT NULL,
  `display_name` varchar(255) NOT NULL,
  `relative_path` varchar(1000) NULL COMMENT '目录导入相对路径',
  `media_type` varchar(20) NOT NULL COMMENT 'IMAGE/VIDEO',
  `description` varchar(2000) NULL,
  `favorite` tinyint NOT NULL DEFAULT 0,
  `width` int NULL,
  `height` int NULL,
  `orientation` int NULL,
  `captured_at` datetime NULL,
  `time_source` varchar(20) NULL COMMENT 'EXIF/VIDEO/FILE/UPLOAD',
  `latitude` decimal(10,7) NULL,
  `longitude` decimal(10,7) NULL,
  `altitude` decimal(10,2) NULL,
  `location_name` varchar(255) NULL,
  `camera_make` varchar(100) NULL,
  `camera_model` varchar(150) NULL,
  `lens_model` varchar(150) NULL,
  `duration_ms` bigint NULL,
  `video_codec` varchar(80) NULL,
  `audio_codec` varchar(80) NULL,
  `bit_rate` bigint NULL,
  `frame_rate` decimal(8,3) NULL,
  `process_status` varchar(20) NOT NULL DEFAULT 'PENDING',
  `process_error` varchar(1000) NULL,
  `metadata_json` json NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `deleted_at` datetime NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_photo_media_space_file` (`space_id`, `original_file_id`),
  INDEX `idx_photo_media_timeline` (`space_id`, `status`, `captured_at`, `id`),
  INDEX `idx_photo_media_type` (`space_id`, `media_type`, `status`, `captured_at`),
  INDEX `idx_photo_media_uploader` (`uploader_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='照片视频媒体';

CREATE TABLE IF NOT EXISTS `photo_album_media` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `album_id` bigint NOT NULL,
  `media_id` bigint NOT NULL,
  `sort_order` int NOT NULL DEFAULT 0,
  `added_by` bigint NOT NULL,
  `added_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_photo_album_media` (`album_id`, `media_id`),
  INDEX `idx_photo_album_media_media` (`media_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='相册媒体关系';

CREATE TABLE IF NOT EXISTS `photo_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `space_id` bigint NOT NULL,
  `name` varchar(100) NOT NULL,
  `normalized_name` varchar(100) NOT NULL,
  `color` varchar(20) NULL,
  `usage_count` int NOT NULL DEFAULT 0,
  `created_by` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_photo_tag_name` (`space_id`, `normalized_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='照片标签';

CREATE TABLE IF NOT EXISTS `photo_media_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `media_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  `created_by` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_photo_media_tag` (`media_id`, `tag_id`),
  INDEX `idx_photo_media_tag_tag` (`tag_id`, `media_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='媒体标签关系';

CREATE TABLE IF NOT EXISTS `photo_media_derivative` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `media_id` bigint NOT NULL,
  `type` varchar(30) NOT NULL COMMENT 'THUMB_SMALL/THUMB_MEDIUM/VIDEO_POSTER',
  `file_id` bigint NOT NULL,
  `width` int NULL,
  `height` int NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_photo_media_derivative` (`media_id`, `type`),
  INDEX `idx_photo_derivative_file` (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='媒体派生文件';

CREATE TABLE IF NOT EXISTS `photo_import_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `space_id` bigint NOT NULL,
  `album_id` bigint NULL,
  `created_by` bigint NOT NULL,
  `source_type` varchar(20) NOT NULL DEFAULT 'FILES',
  `directory_policy` varchar(30) NULL,
  `total_count` int NOT NULL DEFAULT 0,
  `success_count` int NOT NULL DEFAULT 0,
  `skipped_count` int NOT NULL DEFAULT 0,
  `failed_count` int NOT NULL DEFAULT 0,
  `status` varchar(20) NOT NULL DEFAULT 'CREATED',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `finish_time` datetime NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_photo_import_user` (`created_by`, `status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='相册导入批次';

INSERT INTO `sys_menu`
  (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT 0, '家庭相册', '/photo', 'photo/index', 1, 'photo:album:list', 'PictureFilled', 2, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `path` = '/photo');

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, m.id FROM `sys_menu` m
WHERE m.path = '/photo'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id = 1 AND rm.menu_id = m.id
  );
