package com.quinn.hboot.schedule.quartz;

import com.quinn.hboot.schedule.service.ScheduledJobExecutionService;
import com.quinn.hboot.utils.SpringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

/** Quartz bridge. Business execution remains in a Spring service for logging and transactions. */
public class ManagedQuartzJob implements Job {

    public static final String JOB_ID = "jobId";
    public static final String TRIGGER_TYPE = "triggerType";

    @Override
    public void execute(JobExecutionContext context) {
        Long jobId = context.getMergedJobDataMap().getLongValue(JOB_ID);
        String triggerType = context.getMergedJobDataMap().getString(TRIGGER_TYPE);
        SpringUtils.getBean(ScheduledJobExecutionService.class)
            .execute(jobId, triggerType == null ? "SCHEDULED" : triggerType);
    }
}
