package com.quinn.hboot.system.backup.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quinn.hboot.system.backup.model.query.BackupRecordQuery;
import com.quinn.hboot.system.backup.model.vo.BackupRecordVO;
import jakarta.servlet.http.HttpServletResponse;

public interface BackupService {

    IPage<BackupRecordVO> page(BackupRecordQuery query);

    BackupRecordVO get(Long id);

    Long createDatabaseBackup();

    void verify(Long id);

    void download(Long id, HttpServletResponse response) throws Exception;

    void restore(Long id, String confirmation);
}
