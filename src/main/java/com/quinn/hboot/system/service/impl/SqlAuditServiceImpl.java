package com.quinn.hboot.system.service.impl;

import com.alibaba.druid.stat.DruidStatManagerFacade;
import com.quinn.hboot.config.properties.DruidMonitorProperties;
import com.quinn.hboot.config.properties.SqlAuditProperties;
import com.quinn.hboot.system.service.SqlAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SqlAuditServiceImpl implements SqlAuditService {

    private final JdbcTemplate jdbcTemplate;
    private final DruidMonitorProperties druidMonitorProperties;
    private final SqlAuditProperties sqlAuditProperties;

    @Override
    public Map<String, Object> getAuditReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("slowSqlConfig", buildSlowSqlConfig());
        report.put("dataSources", buildDataSourceStats());
        report.put("topSlowSql", buildTopSlowSql());
        report.put("indexAudit", buildIndexAudit());
        return report;
    }

    private Map<String, Object> buildSlowSqlConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("monitorEnabled", druidMonitorProperties.isEnabled());
        config.put("logSlowSql", druidMonitorProperties.isLogSlowSql());
        config.put("mergeSql", druidMonitorProperties.isMergeSql());
        config.put("slowSqlMillis", druidMonitorProperties.getSlowSqlMillis());
        config.put("topN", sqlAuditProperties.getTopN());
        return config;
    }

    private List<Map<String, Object>> buildDataSourceStats() {
        return DruidStatManagerFacade.getInstance().getDataSourceStatDataList();
    }

    private List<Map<String, Object>> buildTopSlowSql() {
        List<Map<String, Object>> allStats = new ArrayList<>();
        List<Map<String, Object>> dataSources = DruidStatManagerFacade.getInstance().getDataSourceStatDataList();
        for (Map<String, Object> dataSource : dataSources) {
            Object identity = dataSource.get("Identity");
            if (identity == null) {
                identity = dataSource.get("identity");
            }
            if (!(identity instanceof Number dataSourceId)) {
                continue;
            }
            allStats.addAll(DruidStatManagerFacade.getInstance().getSqlStatDataList(dataSourceId.intValue()));
        }

        return allStats.stream()
            .sorted(Comparator
                .comparingLong((Map<String, Object> item) -> getLong(item, "ExecuteMillisMax", "MaxTimespan", "executeMillisMax"))
                .reversed()
                .thenComparing(Comparator
                    .comparingLong((Map<String, Object> item) -> getLong(item, "ExecuteCount", "executeCount"))
                    .reversed())
            )
            .limit(Math.max(sqlAuditProperties.getTopN(), 1))
            .map(this::simplifySqlStat)
            .toList();
    }

    private Map<String, Object> simplifySqlStat(Map<String, Object> source) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("sql", firstNonNull(source, "SQL", "sql"));
        item.put("executeCount", getLong(source, "ExecuteCount", "executeCount"));
        item.put("executeMillisMax", getLong(source, "ExecuteMillisMax", "MaxTimespan", "executeMillisMax"));
        item.put("executeMillisTotal", getLong(source, "ExecuteMillisTotal", "executeMillisTotal"));
        item.put("errorCount", getLong(source, "ErrorCount", "executeErrorCount"));
        item.put("lastSlowParameters", firstNonNull(source, "LastSlowParameters", "lastSlowParameters"));
        item.put("dataSource", firstNonNull(source, "DataSource", "dataSource"));
        item.put("lastExecuteTime", firstNonNull(source, "LastTime", "ExecuteLastStartTime", "executeLastStartTime"));
        return item;
    }

    private List<Map<String, Object>> buildIndexAudit() {
        String schema = jdbcTemplate.queryForObject("SELECT current_schema()", String.class);
        List<IndexRecommendation> recommendations = List.of(
            new IndexRecommendation("sys_user", "idx_sys_user_dept_status", List.of("dept_id", "status"), false, "用户分页和组织维度筛选"),
            new IndexRecommendation("sys_menu", "idx_sys_menu_parent_sort_status", List.of("parent_id", "sort", "status"), false, "菜单树和可见菜单排序"),
            new IndexRecommendation("sys_role_menu", "idx_sys_role_menu_menu_id_role_id", List.of("menu_id", "role_id"), false, "菜单反查角色与权限联查"),
            new IndexRecommendation("sys_user_role", "idx_sys_user_role_role_id_user_id", List.of("role_id", "user_id"), false, "角色反查用户"),
            new IndexRecommendation("sys_file", "idx_sys_file_status_create_time", List.of("status", "create_time"), false, "文件分页和时间倒序"),
            new IndexRecommendation("flow_instance", "idx_flow_instance_business_id", List.of("business_id"), false, "业务单据定位流程实例"),
            new IndexRecommendation("flow_instance", "idx_flow_instance_create_by_status", List.of("create_by", "flow_status"), false, "待办/已办/发起流程筛选")
        );

        Map<String, Map<String, Set<String>>> existingIndexMap = loadExistingIndexes(schema);
        List<Map<String, Object>> audit = new ArrayList<>();
        for (IndexRecommendation recommendation : recommendations) {
            Map<String, Set<String>> tableIndexes = existingIndexMap.getOrDefault(recommendation.tableName(), Map.of());
            boolean exists = tableIndexes.entrySet().stream()
                .anyMatch(entry -> normalizeColumns(entry.getValue()).equals(normalizeColumns(recommendation.columns())));

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("tableName", recommendation.tableName());
            item.put("indexName", recommendation.indexName());
            item.put("columns", recommendation.columns());
            item.put("unique", recommendation.unique());
            item.put("reason", recommendation.reason());
            item.put("exists", exists);
            item.put("existingIndexes", tableIndexes);
            item.put("ddl", buildIndexDdl(recommendation));
            audit.add(item);
        }
        return audit;
    }

    private Map<String, Map<String, Set<String>>> loadExistingIndexes(String schema) {
        String columnSql = """
            SELECT attr.attrelid AS table_oid,
                   attr.attnum AS attribute_number,
                   attr.attname AS column_name
            FROM pg_catalog.pg_attribute attr
            JOIN pg_catalog.pg_class tbl ON tbl.oid = attr.attrelid
            JOIN pg_catalog.pg_namespace ns ON ns.oid = tbl.relnamespace
            WHERE ns.nspname = ?
              AND attr.attnum > 0
              AND attr.attisdropped = FALSE
            """;
        Map<Long, Map<Integer, String>> columnsByTable = new LinkedHashMap<>();
        for (Map<String, Object> row : jdbcTemplate.queryForList(columnSql, schema)) {
            long tableOid = getLong(row, "table_oid");
            int attributeNumber = (int) getLong(row, "attribute_number");
            String columnName = Objects.toString(row.get("column_name"), "");
            columnsByTable
                .computeIfAbsent(tableOid, key -> new LinkedHashMap<>())
                .put(attributeNumber, columnName);
        }

        String indexSql = """
            SELECT tbl.oid AS table_oid,
                   tbl.relname AS table_name,
                   idx.relname AS index_name,
                   CAST(index_meta.indkey AS VARCHAR) AS attribute_numbers,
                   index_meta.indnkeyatts AS key_column_count
            FROM pg_catalog.pg_index index_meta
            JOIN pg_catalog.pg_class idx ON idx.oid = index_meta.indexrelid
            JOIN pg_catalog.pg_class tbl ON tbl.oid = index_meta.indrelid
            JOIN pg_catalog.pg_namespace ns ON ns.oid = tbl.relnamespace
            WHERE ns.nspname = ?
            ORDER BY tbl.relname, idx.relname
            """;

        Map<String, Map<String, Set<String>>> result = new LinkedHashMap<>();
        for (Map<String, Object> row : jdbcTemplate.queryForList(indexSql, schema)) {
            long tableOid = getLong(row, "table_oid");
            String tableName = Objects.toString(row.get("table_name"), "");
            String indexName = Objects.toString(row.get("index_name"), "");
            String attributeNumbers = Objects.toString(row.get("attribute_numbers"), "").trim();
            int keyColumnCount = (int) getLong(row, "key_column_count");
            Map<Integer, String> tableColumns = columnsByTable.getOrDefault(tableOid, Map.of());
            Set<String> indexColumns = result
                .computeIfAbsent(tableName, key -> new LinkedHashMap<>())
                .computeIfAbsent(indexName, key -> new LinkedHashSet<>());

            if (attributeNumbers.isEmpty()) {
                continue;
            }
            String[] attributes = attributeNumbers.split("\\s+");
            for (int i = 0; i < Math.min(keyColumnCount, attributes.length); i++) {
                try {
                    String columnName = tableColumns.get(Integer.parseInt(attributes[i]));
                    if (columnName != null) {
                        indexColumns.add(columnName);
                    }
                } catch (NumberFormatException ignored) {
                    // Ignore a malformed catalog vector item instead of failing the entire audit report.
                }
            }
        }
        return result;
    }

    private String buildIndexDdl(IndexRecommendation recommendation) {
        String prefix = recommendation.unique() ? "CREATE UNIQUE INDEX " : "CREATE INDEX ";
        return prefix + recommendation.indexName()
            + " ON " + recommendation.tableName()
            + " (" + String.join(", ", recommendation.columns()) + ");";
    }

    private long getLong(Map<String, Object> source, String... keys) {
        Object value = firstNonNull(source, keys);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private Object firstNonNull(Map<String, Object> source, String... keys) {
        for (String key : keys) {
            if (source.containsKey(key) && source.get(key) != null) {
                return source.get(key);
            }
        }
        return null;
    }

    private String normalizeColumns(Iterable<String> columns) {
        StringBuilder builder = new StringBuilder();
        for (String column : columns) {
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(column);
        }
        return builder.toString();
    }

    private record IndexRecommendation(
        String tableName,
        String indexName,
        List<String> columns,
        boolean unique,
        String reason
    ) {
    }
}
