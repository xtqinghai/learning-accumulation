package com.quinn.hboot.system.backup.model.query;

import com.quinn.hboot.common.base.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BackupRecordQuery extends BasePageQuery {

    private String backupName;
    private Integer status;
}
