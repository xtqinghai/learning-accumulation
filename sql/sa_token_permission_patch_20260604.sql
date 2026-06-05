-- Sa-Token permission patch
-- Date: 2026-06-04
-- Purpose:
-- 1. Add missing permission codes for existing system menus.
-- 2. Add button-permission menu nodes (type = 2).
-- 3. Bind those button permissions to roles that already own the parent page menu.

START TRANSACTION;

-- 1) Backfill query permissions on existing page menus
UPDATE `sys_menu`
SET `permission` = 'system:post:query'
WHERE `id` = 33
  AND (`permission` IS NULL OR `permission` = '');

UPDATE `sys_menu`
SET `permission` = 'system:file:query'
WHERE `id` = 17
  AND (`permission` IS NULL OR `permission` = '');

UPDATE `sys_menu`
SET `permission` = 'system:cms-content:query'
WHERE `id` = 18
  AND (`permission` IS NULL OR `permission` = '');

UPDATE `sys_menu`
SET `permission` = 'system:login-log:query'
WHERE `id` = 9
  AND (`permission` IS NULL OR `permission` = '');

UPDATE `sys_menu`
SET `permission` = 'system:oper-log:query'
WHERE `id` = 10
  AND (`permission` IS NULL OR `permission` = '');

-- 2) Insert button permission menus if missing
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT 33, '新增岗位', NULL, NULL, 2, 'system:post:add', NULL, 1, 1, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `parent_id` = 33 AND `permission` = 'system:post:add'
);

INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT 33, '编辑岗位', NULL, NULL, 2, 'system:post:edit', NULL, 2, 1, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `parent_id` = 33 AND `permission` = 'system:post:edit'
);

INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT 33, '删除岗位', NULL, NULL, 2, 'system:post:delete', NULL, 3, 1, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `parent_id` = 33 AND `permission` = 'system:post:delete'
);

INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT 17, '上传文件', NULL, NULL, 2, 'system:file:add', NULL, 1, 1, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `parent_id` = 17 AND `permission` = 'system:file:add'
);

INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT 17, '编辑文件', NULL, NULL, 2, 'system:file:edit', NULL, 2, 1, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `parent_id` = 17 AND `permission` = 'system:file:edit'
);

INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT 17, '删除文件', NULL, NULL, 2, 'system:file:delete', NULL, 3, 1, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `parent_id` = 17 AND `permission` = 'system:file:delete'
);

INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT 18, '新增内容', NULL, NULL, 2, 'system:cms-content:add', NULL, 1, 1, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `parent_id` = 18 AND `permission` = 'system:cms-content:add'
);

INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT 18, '编辑内容', NULL, NULL, 2, 'system:cms-content:edit', NULL, 2, 1, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `parent_id` = 18 AND `permission` = 'system:cms-content:edit'
);

INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT 18, '删除内容', NULL, NULL, 2, 'system:cms-content:delete', NULL, 3, 1, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `parent_id` = 18 AND `permission` = 'system:cms-content:delete'
);

INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT 9, '新增登录日志', NULL, NULL, 2, 'system:login-log:add', NULL, 1, 1, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `parent_id` = 9 AND `permission` = 'system:login-log:add'
);

INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT 9, '编辑登录日志', NULL, NULL, 2, 'system:login-log:edit', NULL, 2, 1, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `parent_id` = 9 AND `permission` = 'system:login-log:edit'
);

INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT 9, '删除登录日志', NULL, NULL, 2, 'system:login-log:delete', NULL, 3, 1, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `parent_id` = 9 AND `permission` = 'system:login-log:delete'
);

INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT 10, '新增操作日志', NULL, NULL, 2, 'system:oper-log:add', NULL, 1, 1, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `parent_id` = 10 AND `permission` = 'system:oper-log:add'
);

INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT 10, '编辑操作日志', NULL, NULL, 2, 'system:oper-log:edit', NULL, 2, 1, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `parent_id` = 10 AND `permission` = 'system:oper-log:edit'
);

INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT 10, '删除操作日志', NULL, NULL, 2, 'system:oper-log:delete', NULL, 3, 1, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `parent_id` = 10 AND `permission` = 'system:oper-log:delete'
);

-- 3) Grant newly created permission menus to any role that already owns the parent page menu
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT rm.`role_id`, m.`id`
FROM `sys_role_menu` rm
JOIN `sys_menu` m ON m.`parent_id` = 33
WHERE rm.`menu_id` = 33
  AND m.`permission` IN ('system:post:add', 'system:post:edit', 'system:post:delete')
  AND NOT EXISTS (
      SELECT 1 FROM `sys_role_menu` x
      WHERE x.`role_id` = rm.`role_id` AND x.`menu_id` = m.`id`
  );

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT rm.`role_id`, m.`id`
FROM `sys_role_menu` rm
JOIN `sys_menu` m ON m.`parent_id` = 17
WHERE rm.`menu_id` = 17
  AND m.`permission` IN ('system:file:add', 'system:file:edit', 'system:file:delete')
  AND NOT EXISTS (
      SELECT 1 FROM `sys_role_menu` x
      WHERE x.`role_id` = rm.`role_id` AND x.`menu_id` = m.`id`
  );

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT rm.`role_id`, m.`id`
FROM `sys_role_menu` rm
JOIN `sys_menu` m ON m.`parent_id` = 18
WHERE rm.`menu_id` = 18
  AND m.`permission` IN ('system:cms-content:add', 'system:cms-content:edit', 'system:cms-content:delete')
  AND NOT EXISTS (
      SELECT 1 FROM `sys_role_menu` x
      WHERE x.`role_id` = rm.`role_id` AND x.`menu_id` = m.`id`
  );

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT rm.`role_id`, m.`id`
FROM `sys_role_menu` rm
JOIN `sys_menu` m ON m.`parent_id` = 9
WHERE rm.`menu_id` = 9
  AND m.`permission` IN ('system:login-log:add', 'system:login-log:edit', 'system:login-log:delete')
  AND NOT EXISTS (
      SELECT 1 FROM `sys_role_menu` x
      WHERE x.`role_id` = rm.`role_id` AND x.`menu_id` = m.`id`
  );

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT rm.`role_id`, m.`id`
FROM `sys_role_menu` rm
JOIN `sys_menu` m ON m.`parent_id` = 10
WHERE rm.`menu_id` = 10
  AND m.`permission` IN ('system:oper-log:add', 'system:oper-log:edit', 'system:oper-log:delete')
  AND NOT EXISTS (
      SELECT 1 FROM `sys_role_menu` x
      WHERE x.`role_id` = rm.`role_id` AND x.`menu_id` = m.`id`
  );

COMMIT;
