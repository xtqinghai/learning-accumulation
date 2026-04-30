SET NAMES utf8mb4;

SET @schema_name = DATABASE();

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = @schema_name
          AND table_name = 'flow_definition'
          AND column_name = 'model_value'
    ),
    'SELECT 1',
    'ALTER TABLE `flow_definition` ADD COLUMN `model_value` varchar(40) NOT NULL DEFAULT ''CLASSICS'' COMMENT ''设计器模型（CLASSICS经典模型 MIMIC仿钉钉模型）'' AFTER `flow_name`'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = @schema_name
          AND table_name = 'flow_definition'
          AND column_name = 'create_by'
    ),
    'SELECT 1',
    'ALTER TABLE `flow_definition` ADD COLUMN `create_by` varchar(64) NULL DEFAULT NULL COMMENT ''创建人'' AFTER `create_time`'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = @schema_name
          AND table_name = 'flow_definition'
          AND column_name = 'update_by'
    ),
    'SELECT 1',
    'ALTER TABLE `flow_definition` ADD COLUMN `update_by` varchar(64) NULL DEFAULT NULL COMMENT ''更新人'' AFTER `update_time`'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = @schema_name
          AND table_name = 'flow_node'
          AND column_name = 'create_by'
    ),
    'SELECT 1',
    'ALTER TABLE `flow_node` ADD COLUMN `create_by` varchar(64) NULL DEFAULT NULL COMMENT ''创建人'' AFTER `create_time`'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = @schema_name
          AND table_name = 'flow_node'
          AND column_name = 'update_by'
    ),
    'SELECT 1',
    'ALTER TABLE `flow_node` ADD COLUMN `update_by` varchar(64) NULL DEFAULT NULL COMMENT ''更新人'' AFTER `update_time`'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE `flow_node`
    MODIFY COLUMN `node_ratio` varchar(200) NULL DEFAULT NULL COMMENT '流程签署比例值';

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = @schema_name
          AND table_name = 'flow_node'
          AND column_name = 'handler_type'
    ),
    'ALTER TABLE `flow_node` DROP COLUMN `handler_type`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = @schema_name
          AND table_name = 'flow_node'
          AND column_name = 'handler_path'
    ),
    'ALTER TABLE `flow_node` DROP COLUMN `handler_path`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = @schema_name
          AND table_name = 'flow_skip'
          AND column_name = 'create_by'
    ),
    'SELECT 1',
    'ALTER TABLE `flow_skip` ADD COLUMN `create_by` varchar(64) NULL DEFAULT NULL COMMENT ''创建人'' AFTER `create_time`'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = @schema_name
          AND table_name = 'flow_skip'
          AND column_name = 'update_by'
    ),
    'SELECT 1',
    'ALTER TABLE `flow_skip` ADD COLUMN `update_by` varchar(64) NULL DEFAULT NULL COMMENT ''更新人'' AFTER `update_time`'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = @schema_name
          AND table_name = 'flow_instance'
          AND column_name = 'update_by'
    ),
    'SELECT 1',
    'ALTER TABLE `flow_instance` ADD COLUMN `update_by` varchar(64) NULL DEFAULT NULL COMMENT ''更新人'' AFTER `update_time`'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = @schema_name
          AND table_name = 'flow_task'
          AND column_name = 'create_by'
    ),
    'SELECT 1',
    'ALTER TABLE `flow_task` ADD COLUMN `create_by` varchar(64) NULL DEFAULT NULL COMMENT ''创建人'' AFTER `create_time`'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = @schema_name
          AND table_name = 'flow_task'
          AND column_name = 'update_by'
    ),
    'SELECT 1',
    'ALTER TABLE `flow_task` ADD COLUMN `update_by` varchar(64) NULL DEFAULT NULL COMMENT ''更新人'' AFTER `update_time`'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE `flow_his_task`
    MODIFY COLUMN `collaborator` varchar(500) NULL DEFAULT NULL COMMENT '协作人';

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = @schema_name
          AND table_name = 'flow_user'
          AND column_name = 'update_by'
    ),
    'SELECT 1',
    'ALTER TABLE `flow_user` ADD COLUMN `update_by` varchar(64) NULL DEFAULT NULL COMMENT ''更新人'' AFTER `update_time`'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = @schema_name
          AND table_name = 'flow_user'
          AND index_name = 'user_associated'
    ),
    'SELECT 1',
    'ALTER TABLE `flow_user` ADD INDEX `user_associated`(`associated` ASC) USING BTREE'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `flow_skip`
SET `skip_condition` = REPLACE(`skip_condition`, 'notNike', 'notLike')
WHERE `skip_condition` LIKE '%notNike%';

UPDATE `sys_menu`
SET `permission` = 'workflow:definition:list'
WHERE `component` = 'workflow/processDefinition/index';

UPDATE `sys_menu`
SET `permission` = 'workflow:instance:list'
WHERE `component` = 'workflow/processInstance/index';

UPDATE `sys_menu`
SET `permission` = 'workflow:instance:currentList'
WHERE `component` = 'workflow/task/myDocument';

DELETE FROM `sys_menu`
WHERE `component` = 'workflow/processDefinition/design'
   OR `path` = '/design/index';
