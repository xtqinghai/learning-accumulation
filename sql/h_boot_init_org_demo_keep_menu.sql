SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 说明：
-- 1. 本脚本不会修改 sys_menu 菜单表数据
-- 2. 会重建组织、岗位、角色、用户及其关系
-- 3. 默认测试密码统一为：123456
-- 4. 密码使用当前项目已可用的 BCrypt 哈希

-- ------------------------------------------------------------
-- 0. 确保新任职关系表存在
-- ------------------------------------------------------------
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
    UNIQUE KEY `uk_user_primary` (`user_id`, `dept_id`, `post_id`, `job_type`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='用户任职关系表';

-- ------------------------------------------------------------
-- 1. 清理旧关系数据
-- ------------------------------------------------------------
DELETE FROM `sys_user_org_post`;
DELETE FROM `sys_user_role`;
DELETE FROM `sys_user_post`;
DELETE FROM `sys_role_menu`;
DELETE FROM `sys_user`;
DELETE FROM `sys_role`;
DELETE FROM `sys_post`;
DELETE FROM `sys_dept`;

-- ------------------------------------------------------------
-- 2. 初始化部门
-- ------------------------------------------------------------
INSERT INTO `sys_dept` (`id`, `name`, `parent_id`, `sort`, `status`, `create_time`, `update_time`, `dept_leader`) VALUES
(1,  'XX公司',     0, 1, 1, NOW(), NOW(), NULL),
(2,  '总经办',     1, 1, 1, NOW(), NOW(), NULL),
(3,  '研发中心',   1, 2, 1, NOW(), NOW(), NULL),
(4,  '产品中心',   1, 3, 1, NOW(), NOW(), NULL),
(5,  '人力行政中心', 1, 4, 1, NOW(), NOW(), NULL),
(6,  '财务中心',   1, 5, 1, NOW(), NOW(), NULL),
(7,  '市场中心',   1, 6, 1, NOW(), NOW(), NULL),
(8,  '流程运营组', 1, 7, 1, NOW(), NOW(), NULL),
(9,  '项目交付组', 3, 1, 1, NOW(), NOW(), NULL);

-- ------------------------------------------------------------
-- 3. 初始化岗位
-- ------------------------------------------------------------
INSERT INTO `sys_post` (`id`, `dept_id`, `post_code`, `post_category`, `post_name`, `post_sort`, `status`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES
(1, 2, 'ceo',              'management', '总经理',         1, 1, 1, 1, NOW(), 1, NOW(), '公司负责人'),
(2, 3, 'rd_director',      'management', '研发总监',       2, 1, 1, 1, NOW(), 1, NOW(), '研发负责人'),
(3, 4, 'product_manager',  'business',   '产品经理',       3, 1, 1, 1, NOW(), 1, NOW(), '产品负责人'),
(4, 5, 'hr_manager',       'management', '人事行政经理',   4, 1, 1, 1, NOW(), 1, NOW(), '人事行政负责人'),
(5, 6, 'finance_manager',  'management', '财务经理',       5, 1, 1, 1, NOW(), 1, NOW(), '财务负责人'),
(6, 7, 'sales_manager',    'business',   '市场经理',       6, 1, 1, 1, NOW(), 1, NOW(), '市场负责人'),
(7, 8, 'workflow_admin',   'workflow',   '流程管理员',     7, 1, 1, 1, NOW(), 1, NOW(), '流程运维'),
(8, 9, 'project_manager',  'business',   '项目经理',       8, 1, 1, 1, NOW(), 1, NOW(), '项目交付负责人'),
(9, 3, 'java_dev',         'technical',  'Java开发工程师', 9, 1, 1, 1, NOW(), 1, NOW(), '研发岗位'),
(10,3, 'qa_engineer',      'technical',  '测试工程师',     10,1, 1, 1, NOW(), 1, NOW(), '测试岗位'),
(11,5, 'hr_specialist',    'support',    '人事专员',       11,1, 1, 1, NOW(), 1, NOW(), '人事岗位'),
(12,6, 'finance_specialist','support',   '财务专员',       12,1, 1, 1, NOW(), 1, NOW(), '财务岗位'),
(13,7, 'sales_specialist', 'support',    '市场专员',       13,1, 1, 1, NOW(), 1, NOW(), '市场岗位');

-- ------------------------------------------------------------
-- 4. 初始化角色
-- ------------------------------------------------------------
INSERT INTO `sys_role` (`id`, `name`, `code`, `status`, `remark`, `create_time`, `update_time`, `sort`) VALUES
(1, '超级管理员', 'super-admin',     1, '系统超级管理员', NOW(), NOW(), 1),
(2, '总经理',     'ceo',             1, '公司总经理',     NOW(), NOW(), 2),
(3, '研发负责人', 'rd-director',     1, '研发负责人',     NOW(), NOW(), 3),
(4, '产品经理',   'product-manager', 1, '产品负责人',     NOW(), NOW(), 4),
(5, 'HR经理',     'hr-manager',      1, '人事行政负责人', NOW(), NOW(), 5),
(6, '财务经理',   'finance-manager', 1, '财务负责人',     NOW(), NOW(), 6),
(7, '市场经理',   'sales-manager',   1, '市场负责人',     NOW(), NOW(), 7),
(8, '流程管理员', 'workflow-admin',  1, '流程平台管理员', NOW(), NOW(), 8),
(9, '项目经理',   'project-manager', 1, '项目交付负责人', NOW(), NOW(), 9),
(10,'普通员工',   'employee',        1, '普通员工基础角色', NOW(), NOW(), 10);

-- ------------------------------------------------------------
-- 5. 初始化用户
-- 统一密码：123456
-- ------------------------------------------------------------
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `dept_id`, `status`, `create_time`, `update_time`, `email`, `post_id`, `mobile`, `role_id`) VALUES
(1,   'admin',        '$2a$10$7ARi0T918cXK4JAU0Y/IJuWuqilxcwnoJuFr.IKVLblCe5yV9JgK6', '系统管理员', 8, 1, NOW(), NOW(), 'admin@xx.com',        7,  '13800000000', 1),
(101, 'ceo',          '$2a$10$7ARi0T918cXK4JAU0Y/IJuWuqilxcwnoJuFr.IKVLblCe5yV9JgK6', '王总',       2, 1, NOW(), NOW(), 'ceo@xx.com',          1,  '13800000001', 2),
(102, 'rd_director',  '$2a$10$7ARi0T918cXK4JAU0Y/IJuWuqilxcwnoJuFr.IKVLblCe5yV9JgK6', '李研发',     3, 1, NOW(), NOW(), 'rd@xx.com',           2,  '13800000002', 3),
(103, 'pm_product',   '$2a$10$7ARi0T918cXK4JAU0Y/IJuWuqilxcwnoJuFr.IKVLblCe5yV9JgK6', '张产品',     4, 1, NOW(), NOW(), 'pm@xx.com',           3,  '13800000003', 4),
(104, 'hr_manager',   '$2a$10$7ARi0T918cXK4JAU0Y/IJuWuqilxcwnoJuFr.IKVLblCe5yV9JgK6', '赵人事',     5, 1, NOW(), NOW(), 'hr@xx.com',           4,  '13800000004', 5),
(105, 'finance_mgr',  '$2a$10$7ARi0T918cXK4JAU0Y/IJuWuqilxcwnoJuFr.IKVLblCe5yV9JgK6', '钱财务',     6, 1, NOW(), NOW(), 'finance@xx.com',      5,  '13800000005', 6),
(106, 'sales_mgr',    '$2a$10$7ARi0T918cXK4JAU0Y/IJuWuqilxcwnoJuFr.IKVLblCe5yV9JgK6', '孙市场',     7, 1, NOW(), NOW(), 'sales@xx.com',        6,  '13800000006', 7),
(107, 'flow_admin',   '$2a$10$7ARi0T918cXK4JAU0Y/IJuWuqilxcwnoJuFr.IKVLblCe5yV9JgK6', '流程管理员', 8, 1, NOW(), NOW(), 'flow@xx.com',         7,  '13800000007', 8),
(108, 'project_mgr',  '$2a$10$7ARi0T918cXK4JAU0Y/IJuWuqilxcwnoJuFr.IKVLblCe5yV9JgK6', '周项目',     9, 1, NOW(), NOW(), 'project@xx.com',      8,  '13800000008', 9),
(109, 'dev_user',     '$2a$10$7ARi0T918cXK4JAU0Y/IJuWuqilxcwnoJuFr.IKVLblCe5yV9JgK6', '吴开发',     3, 1, NOW(), NOW(), 'dev@xx.com',          9,  '13800000009', 10),
(110, 'qa_user',      '$2a$10$7ARi0T918cXK4JAU0Y/IJuWuqilxcwnoJuFr.IKVLblCe5yV9JgK6', '郑测试',     3, 1, NOW(), NOW(), 'qa@xx.com',           10, '13800000010', 10),
(111, 'hr_user',      '$2a$10$7ARi0T918cXK4JAU0Y/IJuWuqilxcwnoJuFr.IKVLblCe5yV9JgK6', '冯人事',     5, 1, NOW(), NOW(), 'hr.user@xx.com',      11, '13800000011', 10),
(112, 'finance_user', '$2a$10$7ARi0T918cXK4JAU0Y/IJuWuqilxcwnoJuFr.IKVLblCe5yV9JgK6', '陈财务',     6, 1, NOW(), NOW(), 'finance.user@xx.com', 12, '13800000012', 10);

-- ------------------------------------------------------------
-- 6. 用户角色关系
-- ------------------------------------------------------------
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),
(101, 2),
(102, 3),
(103, 4),
(104, 5),
(105, 6),
(106, 7),
(107, 8),
(108, 9),
(109, 10),
(110, 10),
(111, 10),
(112, 10),
-- 兼职/复合角色示例
(107, 10),
(108, 3);

-- ------------------------------------------------------------
-- 7. 用户岗位关系
-- ------------------------------------------------------------
INSERT INTO `sys_user_post` (`user_id`, `post_id`) VALUES
(1, 7),
(101, 1),
(102, 2),
(103, 3),
(104, 4),
(105, 5),
(106, 6),
(107, 7),
(108, 8),
(109, 9),
(110, 10),
(111, 11),
(112, 12);

-- ------------------------------------------------------------
-- 8. 用户任职关系
-- ------------------------------------------------------------
INSERT INTO `sys_user_org_post` (`id`, `user_id`, `dept_id`, `post_id`, `is_primary`, `job_type`, `status`, `start_time`, `end_time`, `create_time`, `update_time`) VALUES
(1001, 1,   8, 7,  1, 'main',      1, NOW(), NULL, NOW(), NOW()),
(1002, 101, 2, 1,  1, 'main',      1, NOW(), NULL, NOW(), NOW()),
(1003, 102, 3, 2,  1, 'main',      1, NOW(), NULL, NOW(), NOW()),
(1004, 103, 4, 3,  1, 'main',      1, NOW(), NULL, NOW(), NOW()),
(1005, 104, 5, 4,  1, 'main',      1, NOW(), NULL, NOW(), NOW()),
(1006, 105, 6, 5,  1, 'main',      1, NOW(), NULL, NOW(), NOW()),
(1007, 106, 7, 6,  1, 'main',      1, NOW(), NULL, NOW(), NOW()),
(1008, 107, 8, 7,  1, 'main',      1, NOW(), NULL, NOW(), NOW()),
(1009, 108, 9, 8,  1, 'main',      1, NOW(), NULL, NOW(), NOW()),
(1010, 109, 3, 9,  1, 'main',      1, NOW(), NULL, NOW(), NOW()),
(1011, 110, 3, 10, 1, 'main',      1, NOW(), NULL, NOW(), NOW()),
(1012, 111, 5, 11, 1, 'main',      1, NOW(), NULL, NOW(), NOW()),
(1013, 112, 6, 12, 1, 'main',      1, NOW(), NULL, NOW(), NOW()),
-- 兼职示例
(1014, 108, 3, 9,  0, 'part_time', 1, NOW(), NULL, NOW(), NOW()),
(1015, 107, 5, 11, 0, 'part_time', 1, NOW(), NULL, NOW(), NOW());

-- ------------------------------------------------------------
-- 9. 回写部门负责人
-- ------------------------------------------------------------
UPDATE `sys_dept` SET `dept_leader` = 101 WHERE `id` = 1;
UPDATE `sys_dept` SET `dept_leader` = 101 WHERE `id` = 2;
UPDATE `sys_dept` SET `dept_leader` = 102 WHERE `id` = 3;
UPDATE `sys_dept` SET `dept_leader` = 103 WHERE `id` = 4;
UPDATE `sys_dept` SET `dept_leader` = 104 WHERE `id` = 5;
UPDATE `sys_dept` SET `dept_leader` = 105 WHERE `id` = 6;
UPDATE `sys_dept` SET `dept_leader` = 106 WHERE `id` = 7;
UPDATE `sys_dept` SET `dept_leader` = 107 WHERE `id` = 8;
UPDATE `sys_dept` SET `dept_leader` = 108 WHERE `id` = 9;

-- ------------------------------------------------------------
-- 10. 角色菜单关系
-- 只写 sys_role_menu，不动 sys_menu
-- 根据现有菜单 name/path/component 自动匹配
-- ------------------------------------------------------------

-- 超级管理员：拥有全部菜单
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, m.id
FROM sys_menu m;

-- 总经理：首页 + 我的任务 + 工作流查看
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, m.id
FROM sys_menu m
WHERE m.path IN ('/dashboard', '/workflow', '/task', '/taskWaiting', '/taskFinish', '/taskCopyList', '/myDocument', '/taskAllWaiting', '/processInstance', '/leaveEdit/index')
   OR m.component IN ('workflow/task/taskWaiting', 'workflow/task/taskFinish', 'workflow/task/taskCopyList', 'workflow/task/myDocument', 'workflow/task/allTaskWaiting', 'workflow/processInstance/index', 'workflow/leave/index');

-- 研发负责人
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 3, m.id
FROM sys_menu m
WHERE m.path IN ('/dashboard', '/task', '/taskWaiting', '/taskFinish', '/taskCopyList', '/myDocument', '/leaveEdit/index')
   OR m.component IN ('workflow/task/taskWaiting', 'workflow/task/taskFinish', 'workflow/task/taskCopyList', 'workflow/task/myDocument', 'workflow/leave/index');

-- 产品经理
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 4, m.id
FROM sys_menu m
WHERE m.path IN ('/dashboard', '/task', '/taskWaiting', '/taskFinish', '/taskCopyList', '/myDocument', '/leaveEdit/index')
   OR m.component IN ('workflow/task/taskWaiting', 'workflow/task/taskFinish', 'workflow/task/taskCopyList', 'workflow/task/myDocument', 'workflow/leave/index');

-- HR经理
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 5, m.id
FROM sys_menu m
WHERE m.path IN ('/dashboard', '/task', '/taskWaiting', '/taskFinish', '/taskCopyList', '/myDocument', '/leaveEdit/index')
   OR m.component IN ('workflow/task/taskWaiting', 'workflow/task/taskFinish', 'workflow/task/taskCopyList', 'workflow/task/myDocument', 'workflow/leave/index');

-- 财务经理
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 6, m.id
FROM sys_menu m
WHERE m.path IN ('/dashboard', '/task', '/taskWaiting', '/taskFinish', '/taskCopyList', '/myDocument', '/leaveEdit/index')
   OR m.component IN ('workflow/task/taskWaiting', 'workflow/task/taskFinish', 'workflow/task/taskCopyList', 'workflow/task/myDocument', 'workflow/leave/index');

-- 市场经理
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 7, m.id
FROM sys_menu m
WHERE m.path IN ('/dashboard', '/task', '/taskWaiting', '/taskFinish', '/taskCopyList', '/myDocument', '/leaveEdit/index')
   OR m.component IN ('workflow/task/taskWaiting', 'workflow/task/taskFinish', 'workflow/task/taskCopyList', 'workflow/task/myDocument', 'workflow/leave/index');

-- 流程管理员：系统管理 + 工作流 + 我的任务
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 8, m.id
FROM sys_menu m
WHERE m.path LIKE '/system%'
   OR m.path IN ('/dashboard', '/workflow', '/task', '/taskWaiting', '/taskFinish', '/taskCopyList', '/myDocument', '/taskAllWaiting', '/processInstance', '/leaveEdit/index')
   OR m.component LIKE 'system/%'
   OR m.component LIKE 'workflow/%';

-- 项目经理
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 9, m.id
FROM sys_menu m
WHERE m.path IN ('/dashboard', '/task', '/taskWaiting', '/taskFinish', '/taskCopyList', '/myDocument', '/leaveEdit/index')
   OR m.component IN ('workflow/task/taskWaiting', 'workflow/task/taskFinish', 'workflow/task/taskCopyList', 'workflow/task/myDocument', 'workflow/leave/index');

-- 普通员工：首页 + 我的任务 + 请假申请
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 10, m.id
FROM sys_menu m
WHERE m.path IN ('/dashboard', '/task', '/taskWaiting', '/taskFinish', '/taskCopyList', '/myDocument', '/leaveEdit/index')
   OR m.component IN ('workflow/task/taskWaiting', 'workflow/task/taskFinish', 'workflow/task/taskCopyList', 'workflow/task/myDocument', 'workflow/leave/index');

SET FOREIGN_KEY_CHECKS = 1;
