-- PostgreSQL version for update_flow_form_dynamic.sql
-- The PG schema already contains flow_form; this script is idempotent.

INSERT INTO flow_form
    (id, form_code, form_name, version, is_publish, form_type, form_path,
     form_content, ext, create_time, update_time, del_flag, tenant_id)
SELECT 1948000000000000001,
       'GENERIC_DYNAMIC_FORM',
       '通用动态表单',
       '1',
       1,
       0,
       NULL,
       '{"rule":[{"type":"input","field":"title","title":"申请标题","value":"通用动态表单申请","effect":{"required":true}},{"type":"input","field":"reason","title":"申请说明","props":{"type":"textarea","rows":4},"effect":{"required":true}},{"type":"datePicker","field":"applyDate","title":"申请日期","props":{"type":"date","valueFormat":"YYYY-MM-DD"}}],"option":{"submitBtn":false,"resetBtn":false}}',
       NULL,
       now(),
       now(),
       '0',
       '000000'
WHERE NOT EXISTS (
    SELECT 1
    FROM flow_form
    WHERE form_code = 'GENERIC_DYNAMIC_FORM'
      AND version = '1'
      AND del_flag = '0'
);

-- Expose the dynamic form manager under 工作流. The route component is resolved
-- from the frontend's /src/views directory by buildRoutesFromMenus().
INSERT INTO sys_menu
    (parent_id, name, path, component, type, permission, icon, sort, status,
     create_time, update_time)
SELECT 19,
       '表单管理',
       '/workflow/form/index',
       'workflow/form/index',
       1,
       'workflow:form:list',
       'document',
       3,
       1,
       now(),
       now()
WHERE EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE id = 19
      AND type = 0
)
  AND NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE path = '/workflow/form/index'
      AND type = 1
);

-- Reuse the roles that can already manage process definitions. This keeps the
-- new menu idempotent and avoids granting it to unrelated roles.
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT role_menu.role_id, form_menu.id
FROM sys_role_menu role_menu
JOIN sys_menu definition_menu
  ON definition_menu.id = role_menu.menu_id
 AND definition_menu.path = '/processDefinition'
CROSS JOIN sys_menu form_menu
WHERE form_menu.path = '/workflow/form/index'
  AND form_menu.type = 1
ON CONFLICT DO NOTHING;
