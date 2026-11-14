-- TresJS 示例页面菜单。
-- 保持幂等：旧数据库中“系统工具”可能已经存在，仅在缺失时补建。

INSERT INTO "sys_menu"
  ("parent_id", "name", "path", "component", "type", "permission", "icon", "sort", "status", "create_time", "update_time")
SELECT 0, '系统工具', 'tool', NULL, 0, NULL, 'Tools', 0, 1, NOW(), NOW()
WHERE NOT EXISTS (
  SELECT 1
  FROM "sys_menu"
  WHERE "parent_id" = 0
    AND "type" = 0
    AND ("name" = '系统工具' OR "path" IN ('tool', '/tool'))
);

INSERT INTO "sys_menu"
  ("parent_id", "name", "path", "component", "type", "permission", "icon", "sort", "status", "create_time", "update_time")
SELECT parent."id", 'TresJS 示例', '/tool/tres', 'tres/index', 1, NULL, 'Picture', 1, 1, NOW(), NOW()
FROM "sys_menu" parent
WHERE parent."parent_id" = 0
  AND parent."type" = 0
  AND (parent."name" = '系统工具' OR parent."path" IN ('tool', '/tool'))
  AND NOT EXISTS (
    SELECT 1 FROM "sys_menu" WHERE "path" = '/tool/tres'
  )
ORDER BY parent."id"
LIMIT 1;

-- 系统工具目录及其子菜单仅授权超级管理员角色。
INSERT INTO "sys_role_menu" ("role_id", "menu_id")
SELECT 1, menu."id"
FROM "sys_menu" menu
WHERE (
    menu."id" = (
      SELECT parent."id"
      FROM "sys_menu" parent
      WHERE parent."parent_id" = 0
        AND parent."type" = 0
        AND (parent."name" = '系统工具' OR parent."path" IN ('tool', '/tool'))
      ORDER BY parent."id"
      LIMIT 1
    )
    OR menu."path" = '/tool/tres'
  )
  AND NOT EXISTS (
    SELECT 1
    FROM "sys_role_menu" role_menu
    WHERE role_menu."role_id" = 1
      AND role_menu."menu_id" = menu."id"
  );
