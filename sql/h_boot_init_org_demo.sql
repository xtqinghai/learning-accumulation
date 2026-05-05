SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 确保新任职关系表存在
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

-- 2. 清理组织权限演示数据
DELETE FROM `sys_user_org_post` WHERE user_id IN (1, 101, 102, 103, 104, 105, 106, 107, 108);
DELETE FROM `sys_user_role` WHERE user_id IN (1, 101, 102, 103, 104, 105, 106, 107, 108);
DELETE FROM `sys_user_post` WHERE user_id IN (1, 101, 102, 103, 104, 105, 106, 107, 108);
DELETE FROM `sys_user` WHERE id IN (101, 102, 103, 104, 105, 106, 107, 108);

DELETE FROM `sys_role_menu` WHERE role_id IN (1, 11, 12, 13, 14, 15, 16, 17);
DELETE FROM `sys_role` WHERE id IN (11, 12, 13, 14, 15, 16, 17);

DELETE FROM `sys_post` WHERE id IN (11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
DELETE FROM `sys_dept` WHERE id IN (11, 12, 13, 14, 15, 16, 17, 18);

-- 3. 部门
INSERT INTO `sys_dept` (`id`, `name`, `parent_id`, `sort`, `status`, `create_time`, `update_time`, `dept_leader`) VALUES
(11, '测试集团', 0, 1, 1, NOW(), NOW(), 1),
(12, '总经办', 11, 1, 1, NOW(), NOW(), 1),
(13, '研发中心', 11, 2, 1, NOW(), NOW(), 102),
(14, '产品中心', 11, 3, 1, NOW(), NOW(), 103),
(15, '人力行政中心', 11, 4, 1, NOW(), NOW(), 104),
(16, '财务中心', 11, 5, 1, NOW(), NOW(), 105),
(17, '流程运营组', 11, 6, 1, NOW(), NOW(), 106),
(18, '项目交付组', 13, 1, 1, NOW(), NOW(), 107);

-- 4. 岗位
INSERT INTO `sys_post` (`id`, `dept_id`, `post_code`, `post_category`, `post_name`, `post_sort`, `status`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES
(11, 12, 'ceo', 'management', '总经理', 1, 1, 11, 1, NOW(), 1, NOW(), '集团最高负责人'),
(12, 13, 'rd_director', 'management', '研发总监', 2, 1, 11, 1, NOW(), 1, NOW(), '研发负责人'),
(13, 14, 'product_manager', 'business', '产品经理', 3, 1, 11, 1, NOW(), 1, NOW(), '产品负责人'),
(14, 15, 'hr_manager', 'management', '人事行政经理', 4, 1, 11, 1, NOW(), 1, NOW(), '人事负责人'),
(15, 16, 'finance_manager', 'management', '财务经理', 5, 1, 11, 1, NOW(), 1, NOW(), '财务负责人'),
(16, 17, 'workflow_admin', 'workflow', '流程管理员', 6, 1, 11, 1, NOW(), 1, NOW(), '流程平台运维'),
(17, 18, 'project_manager', 'business', '项目经理', 7, 1, 11, 1, NOW(), 1, NOW(), '项目交付负责人'),
(18, 13, 'java_dev', 'technical', 'Java开发工程师', 8, 1, 11, 1, NOW(), 1, NOW(), '研发岗位'),
(19, 13, 'qa_engineer', 'technical', '测试工程师', 9, 1, 11, 1, NOW(), 1, NOW(), '测试岗位'),
(20, 15, 'hr_specialist', 'support', '人事专员', 10, 1, 11, 1, NOW(), 1, NOW(), '人事岗位');

-- 5. 角色
INSERT INTO `sys_role` (`id`, `name`, `code`, `status`, `remark`, `create_time`, `update_time`, `sort`) VALUES
(1, '超级管理员', 'super-admin', 1, '系统超级管理员', NOW(), NOW(), 1),
(11, '总经理', 'ceo', 1, '集团总经理角色', NOW(), NOW(), 11),
(12, '研发负责人', 'rd-director', 1, '研发负责人角色', NOW(), NOW(), 12),
(13, '产品经理', 'product-manager', 1, '产品管理角色', NOW(), NOW(), 13),
(14, 'HR经理', 'hr-manager', 1, '人事行政角色', NOW(), NOW(), 14),
(15, '财务经理', 'finance-manager', 1, '财务审批角色', NOW(), NOW(), 15),
(16, '流程管理员', 'workflow-admin', 1, '流程设计与运维角色', NOW(), NOW(), 16),
(17, '普通员工', 'employee', 1, '普通员工基础角色', NOW(), NOW(), 17);

-- 6. 角色菜单授权
-- 超级管理员：全部菜单
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, m.id
FROM sys_menu m
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.id
);

-- 总经理：首页、我的任务、工作流相关查看
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(11, 1), (11, 19), (11, 20), (11, 21), (11, 22), (11, 23), (11, 24), (11, 25), (11, 26), (11, 27), (11, 32);

-- 研发负责人
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(12, 1), (12, 19), (12, 20), (12, 21), (12, 22), (12, 24), (12, 25), (12, 27), (12, 32);

-- 产品经理
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(13, 1), (13, 19), (13, 20), (13, 21), (13, 22), (13, 24), (13, 25), (13, 27), (13, 32);

-- HR经理
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(14, 1), (14, 19), (14, 20), (14, 21), (14, 22), (14, 24), (14, 25), (14, 27), (14, 32);

-- 财务经理
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(15, 1), (15, 19), (15, 20), (15, 21), (15, 22), (15, 24), (15, 25), (15, 27), (15, 32);

-- 流程管理员：系统管理 + 工作流
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(16, 1), (16, 2), (16, 3), (16, 4), (16, 5), (16, 6), (16, 17), (16, 18),
(16, 19), (16, 20), (16, 21), (16, 22), (16, 23), (16, 24), (16, 25), (16, 26), (16, 27), (16, 32), (16, 33);

-- 普通员工：首页、请假、我的任务
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(17, 1), (17, 20), (17, 21), (17, 22), (17, 23), (17, 27), (17, 32);

-- 7. 用户
-- 密码统一使用当前 admin 密码哈希
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `dept_id`, `status`, `create_time`, `update_time`, `email`, `post_id`, `mobile`, `role_id`) VALUES
(1,   'admin',        '$2a$10$7ARi0T918cXK4JAU0Y/IJuWuqilxcwnoJuFr.IKVLblCe5yV9JgK6', '系统管理员', 11, 1, NOW(), NOW(), 'admin@test.local',        16, '13800000000', 1),
(101, 'ceo',          '$2a$10$7ARi0T918cXK4JAU0Y/IJuWuqilxcwnoJuFr.IKVLblCe5yV9JgK6', '王总',     12, 1, NOW(), NOW(), 'ceo@test.local',          11, '13800000001', 11),
(102, 'rd_director',  '$2a$10$7ARi0T918cXK4JAU0Y/IJuWuqilxcwnoJuFr.IKVLblCe5yV9JgK6', '李研发',   13, 1, NOW(), NOW(), 'rd.director@test.local',  12, '13800000002', 12),
(103, 'pm_product',   '$2a$10$7ARi0T918cXK4JAU0Y/IJuWuqilxcwnoJuFr.IKVLblCe5yV9JgK6', '张产品',   14, 1, NOW(), NOW(), 'pm@test.local',           13, '13800000003', 13),
(104, 'hr_manager',   '$2a$10$7ARi0T918cXK4JAU0Y/IJuWuqilxcwnoJuFr.IKVLblCe5yV9JgK6', '赵人事',   15, 1, NOW(), NOW(), 'hr@test.local',           14, '13800000004', 14),
(105, 'finance_mgr',  '$2a$10$7ARi0T918cXK4JAU0Y/IJuWuqilxcwnoJuFr.IKVLblCe5yV9JgK6', '钱财务',   16, 1, NOW(), NOW(), 'finance@test.local',      15, '13800000005', 15),
(106, 'flow_admin',   '$2a$10$7ARi0T918cXK4JAU0Y/IJuWuqilxcwnoJuFr.IKVLblCe5yV9JgK6', '流程管理员', 17, 1, NOW(), NOW(), 'flow@test.local',         16, '13800000006', 16),
(107, 'project_mgr',  '$2a$10$7ARi0T918cXK4JAU0Y/IJuWuqilxcwnoJuFr.IKVLblCe5yV9JgK6', '周项目',   18, 1, NOW(), NOW(), 'project@test.local',      17, '13800000007', 17),
(108, 'dev_user',     '$2a$10$7ARi0T918cXK4JAU0Y/IJuWuqilxcwnoJuFr.IKVLblCe5yV9JgK6', '吴开发',   13, 1, NOW(), NOW(), 'dev@test.local',          18, '13800000008', 17);

-- 8. 用户角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),
(101, 11),
(102, 12),
(103, 13),
(104, 14),
(105, 15),
(106, 16),
(107, 17),
(108, 17),
-- 演示兼职/复合授权
(107, 12),
(106, 17);

-- 9. 用户岗位
INSERT INTO `sys_user_post` (`user_id`, `post_id`) VALUES
(1, 16),
(101, 11),
(102, 12),
(103, 13),
(104, 14),
(105, 15),
(106, 16),
(107, 17),
(108, 18);

-- 10. 用户任职关系
INSERT INTO `sys_user_org_post` (`id`, `user_id`, `dept_id`, `post_id`, `is_primary`, `job_type`, `status`, `start_time`, `end_time`, `create_time`, `update_time`) VALUES
(1001, 1,   17, 16, 1, 'main',      1, NOW(), NULL, NOW(), NOW()),
(1002, 101, 12, 11, 1, 'main',      1, NOW(), NULL, NOW(), NOW()),
(1003, 102, 13, 12, 1, 'main',      1, NOW(), NULL, NOW(), NOW()),
(1004, 103, 14, 13, 1, 'main',      1, NOW(), NULL, NOW(), NOW()),
(1005, 104, 15, 14, 1, 'main',      1, NOW(), NULL, NOW(), NOW()),
(1006, 105, 16, 15, 1, 'main',      1, NOW(), NULL, NOW(), NOW()),
(1007, 106, 17, 16, 1, 'main',      1, NOW(), NULL, NOW(), NOW()),
(1008, 107, 18, 17, 1, 'main',      1, NOW(), NULL, NOW(), NOW()),
(1009, 108, 13, 18, 1, 'main',      1, NOW(), NULL, NOW(), NOW()),
-- 兼职示例：项目经理兼任研发中心负责人助理
(1010, 107, 13, 18, 0, 'part_time', 1, NOW(), NULL, NOW(), NOW()),
-- 兼职示例：流程管理员兼任HR专员，可用于流程找人联调
(1011, 106, 15, 20, 0, 'part_time', 1, NOW(), NULL, NOW(), NOW());

-- 11. 回写部门负责人
UPDATE `sys_dept` SET `dept_leader` = 1   WHERE `id` = 11;
UPDATE `sys_dept` SET `dept_leader` = 101 WHERE `id` = 12;
UPDATE `sys_dept` SET `dept_leader` = 102 WHERE `id` = 13;
UPDATE `sys_dept` SET `dept_leader` = 103 WHERE `id` = 14;
UPDATE `sys_dept` SET `dept_leader` = 104 WHERE `id` = 15;
UPDATE `sys_dept` SET `dept_leader` = 105 WHERE `id` = 16;
UPDATE `sys_dept` SET `dept_leader` = 106 WHERE `id` = 17;
UPDATE `sys_dept` SET `dept_leader` = 107 WHERE `id` = 18;

SET FOREIGN_KEY_CHECKS = 1;
