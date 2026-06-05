package com.quinn.hboot.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.quinn.hboot.common.R;
import com.quinn.hboot.common.log.SysLog;
import com.quinn.hboot.system.service.SqlAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "SQL审计接口")
@RestController
@RequestMapping("/api/v1/sql-audit")
@RequiredArgsConstructor
public class SqlAuditController {

    private final SqlAuditService sqlAuditService;

    @Operation(summary = "获取慢SQL和索引审计报告")
    @GetMapping("/report")
    @SaCheckPermission("system:sql-audit:query")
    @SysLog(module = "SQL审计", type = "审计报告")
    public R<Map<String, Object>> getReport() {
        return R.ok(sqlAuditService.getAuditReport());
    }
}
