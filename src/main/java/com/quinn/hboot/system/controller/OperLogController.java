package com.quinn.hboot.system.controller;

import com.quinn.hboot.system.service.OperLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.quinn.hboot.system.model.form.OperLogForm;
import com.quinn.hboot.system.model.query.OperLogQuery;
import com.quinn.hboot.system.model.vo.OperLogVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quinn.hboot.common.R;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * 系统操作日志前端控制层
 *
 * @author yu-qh
 * @since 2025-12-21 19:25
 */
@Tag(name = "系统操作日志接口")
@RestController
@RequestMapping("/api/v1/oper-log")
@RequiredArgsConstructor
public class OperLogController  {

    private final OperLogService operLogService;

    @Operation(summary = "系统操作日志分页列表")
    @GetMapping("/page")
    @SaCheckPermission("system:oper-log:query")
    public R<?> getOperLogPage(OperLogQuery queryParams ) {
        IPage<OperLogVO> result = operLogService.getOperLogPage(queryParams);
        return R.ok(result);
    }

    @Operation(summary = "新增系统操作日志")
    @PostMapping
    @SaCheckPermission("system:oper-log:add")
    public R<Void> saveOperLog(@RequestBody @Valid OperLogForm formData ) {
        boolean result = operLogService.saveOperLog(formData);
        return R.ok();
    }

    @Operation(summary = "获取系统操作日志表单数据")
    @GetMapping("/{id}/form")
    @SaCheckPermission("system:oper-log:edit")
    public R<OperLogForm> getOperLogForm(
        @Parameter(description = "系统操作日志ID") @PathVariable Long id
    ) {
        OperLogForm formData = operLogService.getOperLogFormData(id);
        return R.ok(formData);
    }

    @Operation(summary = "修改系统操作日志")
    @PutMapping(value = "/{id}")
    @SaCheckPermission("system:oper-log:edit")
    public R<Void> updateOperLog(
            @Parameter(description = "系统操作日志ID") @PathVariable Long id,
            @RequestBody @Validated OperLogForm formData
    ) {
        boolean result = operLogService.updateOperLog(id, formData);
        return R.ok();
    }

    @Operation(summary = "删除系统操作日志")
    @DeleteMapping("/{ids}")
    @SaCheckPermission("system:oper-log:delete")
    public R<Void> deleteOperLogs(
        @Parameter(description = "系统操作日志ID，多个以英文逗号(,)分割") @PathVariable String ids
    ) {
        boolean result = operLogService.deleteOperLogs(ids);
        return R.ok();
    }
}
