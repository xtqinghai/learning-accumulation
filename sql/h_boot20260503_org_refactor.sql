SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE `sys_user`
    MODIFY COLUMN `dept_id` bigint NULL COMMENT '主部门ID',
    MODIFY COLUMN `post_id` bigint NULL COMMENT '主岗位ID',
    MODIFY COLUMN `role_id` int NULL COMMENT '兼容字段，保留首个角色ID';

CREATE TABLE IF NOT EXISTS `sys_user_org_post`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     bigint       NOT NULL COMMENT '用户ID',
    `dept_id`     bigint       NOT NULL COMMENT '部门ID',
    `post_id`     bigint       NOT NULL COMMENT '岗位ID',
    `is_primary`  tinyint      NOT NULL DEFAULT 0 COMMENT '是否主任职: 1-是 0-否',
    `job_type`    varchar(32)  NOT NULL DEFAULT 'main' COMMENT '任职类型: main/part_time/temporary/agent/project',
    `status`      tinyint      NOT NULL DEFAULT 1 COMMENT '状态: 1-启用 0-停用',
    `start_time`  datetime     NULL DEFAULT NULL COMMENT '任职开始时间',
    `end_time`    datetime     NULL DEFAULT NULL COMMENT '任职结束时间',
    `create_time` datetime     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_user_primary` (`user_id`, `dept_id`, `post_id`, `job_type`) USING BTREE,
    KEY `idx_uop_user` (`user_id`) USING BTREE,
    KEY `idx_uop_dept` (`dept_id`) USING BTREE,
    KEY `idx_uop_post` (`post_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='用户任职关系表';

INSERT INTO `sys_user_org_post` (`user_id`, `dept_id`, `post_id`, `is_primary`, `job_type`, `status`, `start_time`, `create_time`, `update_time`)
SELECT u.id,
       u.dept_id,
       COALESCE(up.post_id, u.post_id) AS post_id,
       CASE WHEN COALESCE(up.post_id, u.post_id) = u.post_id THEN 1 ELSE 0 END AS is_primary,
       'main',
       1,
       NOW(),
       COALESCE(u.create_time, NOW()),
       COALESCE(u.update_time, NOW())
FROM sys_user u
LEFT JOIN sys_user_post up ON up.user_id = u.id
WHERE u.dept_id IS NOT NULL
  AND COALESCE(up.post_id, u.post_id) IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM sys_user_org_post uop
      WHERE uop.user_id = u.id
        AND uop.dept_id = u.dept_id
        AND uop.post_id = COALESCE(up.post_id, u.post_id)
        AND uop.job_type = 'main'
  );

SET FOREIGN_KEY_CHECKS = 1;
