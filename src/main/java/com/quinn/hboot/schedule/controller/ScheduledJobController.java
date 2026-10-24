package com.quinn.hboot.schedule.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quinn.hboot.common.R;
import com.quinn.hboot.common.log.SysLog;
import com.quinn.hboot.schedule.model.entity.ScheduledJob;
import com.quinn.hboot.schedule.model.entity.ScheduledJobLog;
import com.quinn.hboot.schedule.model.form.ScheduledJobForm;
import com.quinn.hboot.schedule.model.query.ScheduledJobLogQuery;
import com.quinn.hboot.schedule.model.query.ScheduledJobQuery;
import com.quinn.hboot.schedule.model.vo.ScheduledJobHandlerVO;
import com.quinn.hboot.schedule.service.ScheduledJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "定时任务接口")
@RestController
@RequestMapping("/api/v1/scheduled-job")
@RequiredArgsConstructor
public class ScheduledJobController {

    private final ScheduledJobService scheduledJobService;

    @Operation(summary = "定时任务分页列表")
    @GetMapping("/page")
    @SaCheckPermission("system:job:query")
    public R<IPage<ScheduledJob>> page(ScheduledJobQuery query) {
        return R.ok(scheduledJobService.page(query));
    }

    @Operation(summary = "定时任务执行日志")
    @GetMapping("/log/page")
    @SaCheckPermission("system:job:query")
    public R<IPage<ScheduledJobLog>> logPage(ScheduledJobLogQuery query) {
        return R.ok(scheduledJobService.logPage(query));
    }

    @Operation(summary = "可用任务处理器")
    @GetMapping("/handlers")
    @SaCheckPermission("system:job:query")
    public R<List<ScheduledJobHandlerVO>> handlers() {
        return R.ok(scheduledJobService.handlers());
    }

    @Operation(summary = "获取定时任务")
    @GetMapping("/{id}")
    @SaCheckPermission("system:job:query")
    public R<ScheduledJob> get(@PathVariable Long id) {
        return R.ok(scheduledJobService.get(id));
    }

    @Operation(summary = "新增定时任务")
    @PostMapping
    @SaCheckPermission("system:job:add")
    @SysLog(module = "定时任务", type = "新增")
    public R<Long> create(@RequestBody @Valid ScheduledJobForm form) {
        return R.ok(scheduledJobService.create(form));
    }

    @Operation(summary = "修改定时任务")
    @PutMapping("/{id}")
    @SaCheckPermission("system:job:edit")
    @SysLog(module = "定时任务", type = "修改")
    public R<Void> update(@PathVariable Long id, @RequestBody @Valid ScheduledJobForm form) {
        scheduledJobService.update(id, form);
        return R.ok();
    }

    @Operation(summary = "启用或停止定时任务")
    @PutMapping("/{id}/status")
    @SaCheckPermission("system:job:edit")
    @SysLog(module = "定时任务", type = "启停")
    public R<Void> updateStatus(@PathVariable Long id, @Parameter(description = "0 停止，1 启用") @RequestParam Integer status) {
        scheduledJobService.updateStatus(id, status);
        return R.ok();
    }

    @Operation(summary = "立即执行定时任务")
    @PostMapping("/{id}/run")
    @SaCheckPermission("system:job:run")
    @SysLog(module = "定时任务", type = "立即执行")
    public R<Void> runNow(@PathVariable Long id) {
        scheduledJobService.triggerNow(id);
        return R.ok();
    }

    @Operation(summary = "删除定时任务")
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:job:delete")
    @SysLog(module = "定时任务", type = "删除")
    public R<Void> delete(@PathVariable Long id) {
        scheduledJobService.delete(id);
        return R.ok();
    }
}
