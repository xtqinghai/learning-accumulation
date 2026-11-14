-- 动态表单管理初始化脚本（MySQL 8+）
-- flow_form 表由 Warm-Flow 1.8.9 引入；本脚本用于存量库补表并提供一条示例动态表单。
SET @schema_name = DATABASE();

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = @schema_name
          AND table_name = 'flow_form'
    ),
    'SELECT ''flow_form already exists''',
    'CREATE TABLE `flow_form`  (
       `id` bigint UNSIGNED NOT NULL COMMENT ''主键id'',
       `form_code` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT ''表单编码'',
       `form_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT ''表单名称'',
       `version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT ''表单版本'',
       `is_publish` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否发布（0未发布 1已发布 9失效）'',
       `form_type` tinyint(1) NULL DEFAULT 0 COMMENT ''表单类型（0内置表单 存 form_content 1外挂表单 存form_path）'',
       `form_path` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT ''表单路径'',
       `form_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT ''表单内容'',
       `ext` varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT ''表单扩展，用户自行使用'',
       `create_time` datetime NULL DEFAULT NULL COMMENT ''创建时间'',
       `update_time` datetime NULL DEFAULT NULL COMMENT ''更新时间'',
       `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT ''0'' COMMENT ''删除标志'',
       `tenant_id` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT ''租户id'',
       PRIMARY KEY (`id`) USING BTREE
     ) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = ''流程表单表'' ROW_FORMAT = DYNAMIC'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO `flow_form` (`id`, `form_code`, `form_name`, `version`, `is_publish`, `form_type`, `form_path`, `form_content`, `ext`, `create_time`, `update_time`, `del_flag`, `tenant_id`)
SELECT 1948000000000000001, 'GENERIC_DYNAMIC_FORM', '通用动态表单', '1', 1, 0, NULL,
       '{"rule":[{"type":"input","field":"title","title":"申请标题","value":"通用动态表单申请","effect":{"required":true}},{"type":"input","field":"reason","title":"申请说明","props":{"type":"textarea","rows":4},"effect":{"required":true}},{"type":"datePicker","field":"applyDate","title":"申请日期","props":{"type":"date","valueFormat":"YYYY-MM-DD"}}],"option":{"submitBtn":false,"resetBtn":false}}',
       NULL, NOW(), NOW(), '0', '000000'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `flow_form`
    WHERE `form_code` = 'GENERIC_DYNAMIC_FORM' AND `version` = '1' AND `del_flag` = '0'
);

-- 工作流目录下的动态表单管理入口
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `type`, `permission`, `icon`, `sort`, `status`, `create_time`, `update_time`)
SELECT 19, '表单管理', '/workflow/form/index', 'workflow/form/index', 1, 'workflow:form:list', 'document', 3, 1, NOW(), NOW()
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `id` = 19 AND `type` = 0
)
  AND NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `path` = '/workflow/form/index' AND `type` = 1
);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT rm.`role_id`, target.`id`
FROM `sys_role_menu` rm
JOIN `sys_menu` source ON source.`id` = rm.`menu_id` AND source.`path` = '/processDefinition'
CROSS JOIN `sys_menu` target
WHERE target.`path` = '/workflow/form/index'
  AND target.`type` = 1
  AND NOT EXISTS (
      SELECT 1 FROM `sys_role_menu` existing
      WHERE existing.`role_id` = rm.`role_id`
        AND existing.`menu_id` = target.`id`
  );
