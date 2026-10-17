\set ON_ERROR_STOP on
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;

SET escape_string_warning = off;

-- The current h_boot.sql already contains the Warm Flow 1.8.5 columns and indexes.
-- Keep the data corrections from update_warm_flow_1_8_5.sql idempotent.
UPDATE "flow_skip"
SET "skip_condition" = REPLACE("skip_condition", 'notNike', 'notLike')
WHERE "skip_condition" LIKE '%notNike%';

UPDATE "sys_menu" SET "permission" = 'workflow:definition:list'
WHERE "component" = 'workflow/processDefinition/index';
UPDATE "sys_menu" SET "permission" = 'workflow:instance:list'
WHERE "component" = 'workflow/processInstance/index';
UPDATE "sys_menu" SET "permission" = 'workflow:instance:currentList'
WHERE "component" = 'workflow/task/myDocument';
DELETE FROM "sys_menu"
WHERE "component" = 'workflow/processDefinition/design' OR "path" = '/design/index';
