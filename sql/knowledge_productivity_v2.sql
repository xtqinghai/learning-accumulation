-- P1 第二阶段：个人效率与知识积累
-- 适用: MySQL 8.0 / h_boot。请在 knowledge_base_v1.sql 之后执行。

CREATE TABLE IF NOT EXISTS `kb_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `owner_id` bigint NOT NULL,
  `name` varchar(100) NOT NULL,
  `type` varchar(30) NOT NULL DEFAULT 'KNOWLEDGE' COMMENT 'KNOWLEDGE/SNIPPET/DOCUMENT',
  `sort` int NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_kb_category_owner_type_name` (`owner_id`, `type`, `name`),
  INDEX `idx_kb_category_owner_type` (`owner_id`, `type`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='个人内容分类';

CREATE TABLE IF NOT EXISTS `kb_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `owner_id` bigint NOT NULL,
  `name` varchar(60) NOT NULL,
  `color` varchar(20) NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_kb_tag_owner_name` (`owner_id`, `name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='个人标签';

CREATE TABLE IF NOT EXISTS `kb_article_meta` (
  `article_id` bigint NOT NULL,
  `category_id` bigint NULL,
  `owner_id` bigint NOT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`article_id`),
  INDEX `idx_kb_article_meta_owner_category` (`owner_id`, `category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='知识文章扩展元数据';

CREATE TABLE IF NOT EXISTS `kb_article_tag` (
  `article_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  `owner_id` bigint NOT NULL,
  PRIMARY KEY (`article_id`, `tag_id`),
  INDEX `idx_kb_article_tag_owner_tag` (`owner_id`, `tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='知识文章标签关联';

CREATE TABLE IF NOT EXISTS `kb_article_relation` (
  `article_id` bigint NOT NULL,
  `related_article_id` bigint NOT NULL,
  `owner_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`article_id`, `related_article_id`),
  INDEX `idx_kb_article_relation_related` (`related_article_id`, `owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='知识文章双向关联';

CREATE TABLE IF NOT EXISTS `dev_snippet` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `owner_id` bigint NOT NULL,
  `category_id` bigint NULL,
  `title` varchar(200) NOT NULL,
  `language` varchar(30) NOT NULL,
  `content` longtext NOT NULL,
  `description` text NULL,
  `example` longtext NULL,
  `tags` varchar(1000) NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `idx_dev_snippet_owner_language` (`owner_id`, `language`, `deleted`, `update_time`),
  INDEX `idx_dev_snippet_owner_category` (`owner_id`, `category_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='个人代码片段';

CREATE TABLE IF NOT EXISTS `project_document` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `owner_id` bigint NOT NULL,
  `category_id` bigint NULL,
  `project_name` varchar(100) NOT NULL,
  `title` varchar(200) NOT NULL,
  `document_type` varchar(30) NOT NULL COMMENT 'REQUIREMENT/TECHNICAL/DATABASE/API/RELEASE/DECISION',
  `content` longtext NOT NULL,
  `tags` varchar(1000) NULL,
  `status` varchar(20) NOT NULL DEFAULT 'DRAFT',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `idx_project_document_owner_project` (`owner_id`, `project_name`, `deleted`, `update_time`),
  INDEX `idx_project_document_owner_type` (`owner_id`, `document_type`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目文档中心';

CREATE TABLE IF NOT EXISTS `personal_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `owner_id` bigint NOT NULL,
  `title` varchar(200) NOT NULL,
  `description` text NULL,
  `status` varchar(20) NOT NULL DEFAULT 'TODO' COMMENT 'TODO/IN_PROGRESS/DONE/CANCELLED',
  `priority` varchar(20) NOT NULL DEFAULT 'MEDIUM',
  `progress` int NOT NULL DEFAULT 0,
  `plan_date` date NULL,
  `due_time` datetime NULL,
  `recurrence_rule` varchar(200) NULL COMMENT 'DAILY/WEEKLY/MONTHLY or cron-like description',
  `completed_time` datetime NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `idx_personal_task_owner_status_date` (`owner_id`, `status`, `deleted`, `plan_date`, `due_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='个人任务';

CREATE TABLE IF NOT EXISTS `kb_transfer_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `owner_id` bigint NOT NULL,
  `direction` varchar(20) NOT NULL COMMENT 'IMPORT/EXPORT',
  `data_type` varchar(30) NOT NULL COMMENT 'KNOWLEDGE/SNIPPET/DOCUMENT/TASK',
  `format` varchar(20) NOT NULL COMMENT 'JSON/CSV/MARKDOWN/EXCEL',
  `status` varchar(20) NOT NULL COMMENT 'PENDING/RUNNING/SUCCESS/FAILED',
  `total_count` int NOT NULL DEFAULT 0,
  `success_count` int NOT NULL DEFAULT 0,
  `failed_count` int NOT NULL DEFAULT 0,
  `error_message` text NULL,
  `payload` longtext NULL COMMENT '用于失败重试的原始文本',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_kb_transfer_owner` (`owner_id`, `status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='数据导入导出记录';

INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT 0, '开发工作台', '/productivity', 'productivity/index', 1, 'productivity:workspace:list', 'Monitor', 4, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `path` = '/productivity');

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, m.id FROM `sys_menu` m WHERE m.path = '/productivity'
AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id = 1 AND rm.menu_id = m.id);
