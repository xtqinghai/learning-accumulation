package com.quinn.hboot.schedule.service.impl;

import com.quinn.hboot.schedule.handler.ScheduledJobHandler;
import com.quinn.hboot.schedule.handler.ScheduledJobHandlerRegistry;
import com.quinn.hboot.schedule.mapper.ScheduledJobLogMapper;
import com.quinn.hboot.schedule.mapper.ScheduledJobMapper;
import com.quinn.hboot.schedule.model.entity.ScheduledJob;
import com.quinn.hboot.schedule.model.entity.ScheduledJobLog;
import com.quinn.hboot.schedule.service.ScheduledJobExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledJobExecutionServiceImpl implements ScheduledJobExecutionService {

    private static final int ACTIVE = 1;
    private static final int SUCCESS = 1;
    private static final int FAILED = 0;

    private final ScheduledJobMapper scheduledJobMapper;
    private final ScheduledJobLogMapper scheduledJobLogMapper;
    private final ScheduledJobHandlerRegistry handlerRegistry;
    private final Set<Long> runningJobIds = ConcurrentHashMap.newKeySet();

    @Override
    public void execute(Long jobId, String triggerType) {
        ScheduledJob job = scheduledJobMapper.selectById(jobId);
        if (job == null || ("SCHEDULED".equals(triggerType) && job.getStatus() != ACTIVE)) {
            return;
        }
        if (!runningJobIds.add(jobId)) {
            log.warn("Scheduled job {} is already running, skip trigger {}", jobId, triggerType);
            return;
        }

        LocalDateTime startTime = LocalDateTime.now();
        ScheduledJobLog jobLog = new ScheduledJobLog();
        jobLog.setJobId(job.getId());
        jobLog.setJobName(job.getJobName());
        jobLog.setHandlerKey(job.getHandlerKey());
        jobLog.setTriggerType(triggerType);
        jobLog.setStatus(FAILED);
        jobLog.setStartTime(startTime);
        scheduledJobLogMapper.insert(jobLog);

        try {
            ScheduledJobHandler handler = handlerRegistry.getRequired(job.getHandlerKey());
            jobLog.setMessage(handler.execute());
            jobLog.setStatus(SUCCESS);
        } catch (Exception exception) {
            jobLog.setMessage(exception.getMessage());
            jobLog.setErrorStack(stackTrace(exception));
            log.error("Scheduled job execution failed: id={}, handler={}", job.getId(), job.getHandlerKey(), exception);
        } finally {
            LocalDateTime endTime = LocalDateTime.now();
            jobLog.setEndTime(endTime);
            jobLog.setDurationMs(Duration.between(startTime, endTime).toMillis());
            scheduledJobLogMapper.updateById(jobLog);
            runningJobIds.remove(jobId);
        }
    }

    private String stackTrace(Exception exception) {
        StringWriter writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        String value = writer.toString();
        return value.length() > 10000 ? value.substring(0, 10000) : value;
    }
}
