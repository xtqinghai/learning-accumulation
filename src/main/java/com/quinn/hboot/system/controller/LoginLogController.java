package com.quinn.hboot.system.controller;

import com.quinn.hboot.common.R;
import com.quinn.hboot.system.service.LoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.quinn.hboot.system.model.form.LoginLogForm;
import com.quinn.hboot.system.model.query.LoginLogQuery;
import com.quinn.hboot.system.model.vo.LoginLogVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * 系统登录日志前端控制层
 *
 * @author yu-qh
 * @since 2025-12-21 18:35
 */
@Tag(name = "系统登录日志接口")
@RestController
@RequestMapping("/api/v1/login-log")
@RequiredArgsConstructor
public class LoginLogController  {

    private final LoginLogService loginLogService;

    @Operation(summary = "系统登录日志分页列表")
    @GetMapping("/page")
    @SaCheckPermission("system:login-log:query")
    public R<?> getLoginLogPage(LoginLogQuery queryParams ) {
        IPage<LoginLogVO> result = loginLogService.getLoginLogPage(queryParams);
        return R.ok(result);
    }

    @Operation(summary = "新增系统登录日志")
    @PostMapping
    @SaCheckPermission("system:login-log:add")
    public R<Void> saveLoginLog(@RequestBody @Valid LoginLogForm formData ) {
        boolean result = loginLogService.saveLoginLog(formData);
        return R.ok();
    }

    @Operation(summary = "获取系统登录日志表单数据")
    @GetMapping("/{id}/form")
    @SaCheckPermission("system:login-log:edit")
    public R<LoginLogForm> getLoginLogForm(
        @Parameter(description = "系统登录日志ID") @PathVariable Long id
    ) {
        LoginLogForm formData = loginLogService.getLoginLogFormData(id);
        return R.ok(formData);
    }

    @Operation(summary = "修改系统登录日志")
    @PutMapping(value = "/{id}")
    @SaCheckPermission("system:login-log:edit")
    public R<Void> updateLoginLog(
            @Parameter(description = "系统登录日志ID") @PathVariable Long id,
            @RequestBody @Validated LoginLogForm formData
    ) {
        boolean result = loginLogService.updateLoginLog(id, formData);
        return R.ok();
    }

    @Operation(summary = "删除系统登录日志")
    @DeleteMapping("/{ids}")
    @SaCheckPermission("system:login-log:delete")
    public R<Void> deleteLoginLogs(
        @Parameter(description = "系统登录日志ID，多个以英文逗号(,)分割") @PathVariable String ids
    ) {
        boolean result = loginLogService.deleteLoginLogs(ids);
        return R.ok();
    }
}
