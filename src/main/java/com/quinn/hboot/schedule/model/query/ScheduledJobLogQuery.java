package com.quinn.hboot.schedule.model.query;

import com.quinn.hboot.common.base.BasePageQuery;
import lombok.Data;

@Data
public class ScheduledJobLogQuery extends BasePageQuery {

    private Long jobId;
    private Integer status;
}
