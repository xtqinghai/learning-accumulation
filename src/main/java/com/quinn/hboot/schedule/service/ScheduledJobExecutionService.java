package com.quinn.hboot.schedule.service;

public interface ScheduledJobExecutionService {

    void execute(Long jobId, String triggerType);
}
