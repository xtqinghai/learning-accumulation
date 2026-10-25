package com.quinn.hboot.system.backup.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quinn.hboot.system.backup.model.entity.BackupRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BackupRecordMapper extends BaseMapper<BackupRecord> {
}
