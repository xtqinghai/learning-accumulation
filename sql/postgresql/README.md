# PostgreSQL 初始化脚本

这些脚本由当前 MySQL 主快照 `../h_boot.sql` 及其后续业务补丁生成，目标版本为 PostgreSQL 17。

执行顺序由 `import.sql` 管理：

1. 当前主快照（表结构和基础数据）
2. 知识库和家庭相册模块
3. Sa-Token 权限、Warm Flow 数据修正和审计索引
4. 自增序列同步

历史全量快照 `h_boot20260129.sql`、`h_boot20260430.sql`、`h_boot20260505.sql` 不应重复导入。`h_boot_init_org_demo*.sql` 会重置组织数据，也未加入默认初始化流程。

重新生成脚本：

```bash
python3 sql/postgresql/convert_mysql_to_postgresql.py
```

导入到本地数据库：

```bash
createdb h_boot
psql -X -d h_boot -f sql/postgresql/import.sql
```
