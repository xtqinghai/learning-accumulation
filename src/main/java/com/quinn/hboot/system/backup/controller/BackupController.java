package com.quinn.hboot.system.backup.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quinn.hboot.common.R;
import com.quinn.hboot.common.log.SysLog;
import com.quinn.hboot.system.backup.model.query.BackupRecordQuery;
import com.quinn.hboot.system.backup.model.vo.BackupRecordVO;
import com.quinn.hboot.system.backup.service.BackupService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/backup")
@RequiredArgsConstructor
public class BackupController {

    private final BackupService backupService;

    @GetMapping("/page")
    @SaCheckPermission("system:backup:query")
    public R<IPage<BackupRecordVO>> page(BackupRecordQuery query) {
        return R.ok(backupService.page(query));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("system:backup:query")
    public R<BackupRecordVO> get(@PathVariable Long id) {
        return R.ok(backupService.get(id));
    }

    @PostMapping("/database")
    @SaCheckPermission("system:backup:create")
    @SysLog(module = "备份恢复", type = "创建数据库备份")
    public R<Long> createDatabaseBackup() {
        return R.ok(backupService.createDatabaseBackup());
    }

    @PostMapping("/{id}/verify")
    @SaCheckPermission("system:backup:verify")
    @SysLog(module = "备份恢复", type = "校验备份")
    public R<Void> verify(@PathVariable Long id) {
        backupService.verify(id);
        return R.ok();
    }

    @GetMapping("/{id}/download")
    @SaCheckPermission("system:backup:download")
    public void download(@PathVariable Long id, HttpServletResponse response) throws Exception {
        backupService.download(id, response);
    }

    @PostMapping("/{id}/restore")
    @SaCheckPermission("system:backup:restore")
    @SysLog(module = "备份恢复", type = "恢复数据库")
    public R<Void> restore(@PathVariable Long id, @RequestParam String confirmation) {
        backupService.restore(id, confirmation);
        return R.ok();
    }
}
