package com.quinn.hboot.schedule.model.query;

import com.quinn.hboot.common.base.BasePageQuery;
import lombok.Data;

@Data
public class ScheduledJobQuery extends BasePageQuery {

    private String jobName;
    private Integer status;
}
