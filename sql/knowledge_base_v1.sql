-- 个人知识库第一阶段增量脚本
-- 适用: MySQL 8.0 / h_boot
-- 中文模糊搜索首期由应用生成二元词组并使用字段加权 LIKE；FULLTEXT 索引保留作后续优化。

CREATE TABLE IF NOT EXISTS `kb_space` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `owner_id` bigint NOT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_kb_space_owner` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='个人知识空间';

CREATE TABLE IF NOT EXISTS `kb_article` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `space_id` bigint NOT NULL,
  `owner_id` bigint NOT NULL,
  `title` varchar(200) NOT NULL,
  `keywords` varchar(1000) NULL,
  `summary` varchar(2000) NULL,
  `content` longtext NOT NULL COMMENT 'Markdown 原文',
  `search_content` longtext NOT NULL COMMENT '用于搜索的纯文本',
  `status` varchar(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED',
  `source_type` varchar(30) NOT NULL DEFAULT 'MANUAL' COMMENT 'MANUAL/AI_SUMMARY',
  `source_file_id` bigint NULL,
  `current_version_id` bigint NULL,
  `publish_time` datetime NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `idx_kb_article_space_status` (`space_id`, `status`, `deleted`, `update_time`),
  INDEX `idx_kb_article_owner` (`owner_id`, `deleted`),
  FULLTEXT INDEX `ft_kb_article_title` (`title`) WITH PARSER ngram,
  FULLTEXT INDEX `ft_kb_article_keywords` (`keywords`) WITH PARSER ngram,
  FULLTEXT INDEX `ft_kb_article_summary` (`summary`) WITH PARSER ngram,
  FULLTEXT INDEX `ft_kb_article_content` (`search_content`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='知识库文章';

CREATE TABLE IF NOT EXISTS `kb_article_version` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `article_id` bigint NOT NULL,
  `version_no` int NOT NULL,
  `title` varchar(200) NOT NULL,
  `keywords` varchar(1000) NULL,
  `summary` varchar(2000) NULL,
  `content` longtext NOT NULL,
  `content_hash` char(64) NOT NULL,
  `source` varchar(30) NOT NULL DEFAULT 'USER_EDIT',
  `created_by` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_kb_article_version` (`article_id`, `version_no`),
  INDEX `idx_kb_article_version_time` (`article_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='知识库文章版本';

CREATE TABLE IF NOT EXISTS `kb_source_file` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `space_id` bigint NOT NULL,
  `owner_id` bigint NOT NULL,
  `sys_file_id` bigint NOT NULL,
  `article_id` bigint NULL,
  `file_name` varchar(255) NOT NULL,
  `file_ext` varchar(20) NOT NULL,
  `content_type` varchar(150) NULL,
  `file_size` bigint NOT NULL,
  `parsed_text` longtext NULL COMMENT '提取后的正文，仅供预览和总结',
  `status` varchar(30) NOT NULL DEFAULT 'UPLOADED' COMMENT 'UPLOADED/PARSING/PARSED/READY/FAILED',
  `error_message` varchar(2000) NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_kb_source_owner` (`owner_id`, `status`, `create_time`),
  INDEX `idx_kb_source_article` (`article_id`),
  INDEX `idx_kb_source_sys_file` (`sys_file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='知识库原始文件';

CREATE TABLE IF NOT EXISTS `kb_import_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `source_file_id` bigint NOT NULL,
  `owner_id` bigint NOT NULL,
  `article_id` bigint NULL,
  `status` varchar(30) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PARSING/SUMMARIZING/SAVING_ARTICLE/READY/FAILED',
  `progress` int NOT NULL DEFAULT 0,
  `current_stage` varchar(100) NULL,
  `retry_count` int NOT NULL DEFAULT 0,
  `error_message` varchar(2000) NULL,
  `model` varchar(100) NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_kb_import_owner` (`owner_id`, `status`, `create_time`),
  INDEX `idx_kb_import_source` (`source_file_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='知识库文件导入任务';

CREATE TABLE IF NOT EXISTS `kb_article_annotation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `article_id` bigint NOT NULL,
  `article_version_id` bigint NOT NULL,
  `author_id` bigint NOT NULL,
  `source_type` varchar(20) NOT NULL DEFAULT 'MANUAL' COMMENT 'MANUAL/AI_QA',
  `content` text NOT NULL,
  `quoted_text` text NULL,
  `qa_selected_text` text NULL,
  `anchor_type` varchar(20) NOT NULL DEFAULT 'ARTICLE' COMMENT 'ARTICLE/TEXT',
  `anchor_start` int NULL,
  `anchor_end` int NULL,
  `anchor_block_id` varchar(100) NULL,
  `qa_message_id` bigint NULL,
  `pinned` tinyint NOT NULL DEFAULT 0,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_kb_annotation_article` (`article_id`, `article_version_id`, `status`, `pinned`, `create_time`),
  INDEX `idx_kb_annotation_author` (`author_id`, `status`),
  INDEX `idx_kb_annotation_qa` (`qa_message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章批注';

CREATE TABLE IF NOT EXISTS `kb_qa_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `article_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `title` varchar(200) NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_kb_qa_session_user` (`user_id`, `article_id`, `status`, `update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章问答会话';

CREATE TABLE IF NOT EXISTS `kb_qa_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL,
  `role` varchar(20) NOT NULL COMMENT 'user/assistant',
  `content` text NOT NULL,
  `model` varchar(100) NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_kb_qa_message_session` (`session_id`, `create_time`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章问答消息';

INSERT INTO `sys_menu`
  (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT 0, '知识库', '/knowledge', 'knowledge/index', 1, 'knowledge:article:list', 'Reading', 3, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `path` = '/knowledge');

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, m.id FROM `sys_menu` m
WHERE m.path = '/knowledge'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id = 1 AND rm.menu_id = m.id
  );
