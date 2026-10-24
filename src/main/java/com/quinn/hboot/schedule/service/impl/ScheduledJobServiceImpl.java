package com.quinn.hboot.schedule.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.common.exception.BusinessException;
import com.quinn.hboot.schedule.handler.ScheduledJobHandlerRegistry;
import com.quinn.hboot.schedule.mapper.ScheduledJobLogMapper;
import com.quinn.hboot.schedule.mapper.ScheduledJobMapper;
import com.quinn.hboot.schedule.model.entity.ScheduledJob;
import com.quinn.hboot.schedule.model.entity.ScheduledJobLog;
import com.quinn.hboot.schedule.model.form.ScheduledJobForm;
import com.quinn.hboot.schedule.model.query.ScheduledJobLogQuery;
import com.quinn.hboot.schedule.model.query.ScheduledJobQuery;
import com.quinn.hboot.schedule.model.vo.ScheduledJobHandlerVO;
import com.quinn.hboot.schedule.quartz.ManagedQuartzJob;
import com.quinn.hboot.schedule.service.ScheduledJobService;
import com.quinn.hboot.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScheduledJobServiceImpl implements ScheduledJobService {

    private static final int STOPPED = 0;
    private static final int ACTIVE = 1;
    private static final String JOB_GROUP = "managed-jobs";
    private static final String MANUAL_GROUP = "manual-jobs";

    private final ScheduledJobMapper scheduledJobMapper;
    private final ScheduledJobLogMapper scheduledJobLogMapper;
    private final ScheduledJobHandlerRegistry handlerRegistry;
    private final Scheduler scheduler;

    @Override
    public IPage<ScheduledJob> page(ScheduledJobQuery query) {
        return scheduledJobMapper.selectJobPage(query.buildPage(), query);
    }

    @Override
    public IPage<ScheduledJobLog> logPage(ScheduledJobLogQuery query) {
        return scheduledJobLogMapper.selectLogPage(query.buildPage(), query);
    }

    @Override
    public ScheduledJob get(Long id) {
        ScheduledJob job = scheduledJobMapper.selectById(id);
        if (job == null) {
            throw new BusinessException("定时任务不存在");
        }
        return job;
    }

    @Override
    public List<ScheduledJobHandlerVO> handlers() {
        return handlerRegistry.list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ScheduledJobForm form) {
        validate(form, null);
        ScheduledJob job = new ScheduledJob();
        copy(form, job);
        String username = SecurityUtils.getUsername();
        LocalDateTime now = LocalDateTime.now();
        job.setCreator(username);
        job.setUpdater(username);
        job.setCreateTime(now);
        job.setUpdateTime(now);
        scheduledJobMapper.insert(job);
        if (job.getStatus() == ACTIVE) {
            schedule(job);
        }
        return job.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ScheduledJobForm form) {
        ScheduledJob existing = get(id);
        validate(form, id);
        unschedule(id);
        copy(form, existing);
        existing.setUpdater(SecurityUtils.getUsername());
        existing.setUpdateTime(LocalDateTime.now());
        scheduledJobMapper.updateById(existing);
        if (existing.getStatus() == ACTIVE) {
            schedule(existing);
        }
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != STOPPED && status != ACTIVE)) {
            throw new BusinessException("任务状态不合法");
        }
        ScheduledJob job = get(id);
        job.setStatus(status);
        job.setUpdater(SecurityUtils.getUsername());
        job.setUpdateTime(LocalDateTime.now());
        scheduledJobMapper.updateById(job);
        if (status == ACTIVE) {
            schedule(job);
        } else {
            unschedule(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        get(id);
        unschedule(id);
        scheduledJobLogMapper.delete(new LambdaQueryWrapper<ScheduledJobLog>()
            .eq(ScheduledJobLog::getJobId, id));
        scheduledJobMapper.deleteById(id);
    }

    @Override
    public void triggerNow(Long id) {
        get(id);
        try {
            JobDataMap dataMap = dataMap(id, "MANUAL");
            JobDetail detail = JobBuilder.newJob(ManagedQuartzJob.class)
                .withIdentity("manual-" + id + "-" + UUID.randomUUID(), MANUAL_GROUP)
                .usingJobData(dataMap)
                .build();
            Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("manual-trigger-" + UUID.randomUUID(), MANUAL_GROUP)
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withRepeatCount(0))
                .build();
            scheduler.scheduleJob(detail, trigger);
        } catch (SchedulerException exception) {
            throw new BusinessException("立即执行任务失败: " + exception.getMessage());
        }
    }

    @Override
    public void schedule(ScheduledJob job) {
        try {
            JobKey jobKey = jobKey(job.getId());
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
            JobDetail detail = JobBuilder.newJob(ManagedQuartzJob.class)
                .withIdentity(jobKey)
                .usingJobData(dataMap(job.getId(), "SCHEDULED"))
                .build();
            Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey(job.getId()))
                .forJob(detail)
                .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression())
                    .withMisfireHandlingInstructionDoNothing())
                .build();
            scheduler.scheduleJob(detail, trigger);
        } catch (SchedulerException exception) {
            throw new BusinessException("启动定时任务失败: " + exception.getMessage());
        }
    }

    @Override
    public void unschedule(Long jobId) {
        try {
            JobKey jobKey = jobKey(jobId);
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
        } catch (SchedulerException exception) {
            throw new BusinessException("停止定时任务失败: " + exception.getMessage());
        }
    }

    @Override
    public void restoreActiveJobs() {
        List<ScheduledJob> jobs = scheduledJobMapper.selectList(new LambdaQueryWrapper<ScheduledJob>()
            .eq(ScheduledJob::getStatus, ACTIVE));
        for (ScheduledJob job : jobs) {
            try {
                schedule(job);
            } catch (Exception exception) {
                // Invalid historical configuration must not prevent application startup.
            }
        }
    }

    private void validate(ScheduledJobForm form, Long currentId) {
        if (form.getStatus() == null || (form.getStatus() != STOPPED && form.getStatus() != ACTIVE)) {
            throw new BusinessException("任务状态不合法");
        }
        if (!CronExpression.isValidExpression(form.getCronExpression())) {
            throw new BusinessException("Cron 表达式不合法");
        }
        handlerRegistry.getRequired(form.getHandlerKey());
        Long count = scheduledJobMapper.selectCount(new LambdaQueryWrapper<ScheduledJob>()
            .eq(ScheduledJob::getJobName, form.getJobName())
            .ne(currentId != null, ScheduledJob::getId, currentId));
        if (count != null && count > 0) {
            throw new BusinessException("任务名称已存在");
        }
    }

    private void copy(ScheduledJobForm source, ScheduledJob target) {
        target.setJobName(source.getJobName().trim());
        target.setHandlerKey(source.getHandlerKey());
        target.setCronExpression(source.getCronExpression().trim());
        target.setStatus(source.getStatus());
        target.setRemark(StringUtils.hasText(source.getRemark()) ? source.getRemark().trim() : null);
    }

    private JobDataMap dataMap(Long jobId, String triggerType) {
        JobDataMap result = new JobDataMap();
        result.put(ManagedQuartzJob.JOB_ID, jobId);
        result.put(ManagedQuartzJob.TRIGGER_TYPE, triggerType);
        return result;
    }

    private JobKey jobKey(Long jobId) {
        return new JobKey("job-" + jobId, JOB_GROUP);
    }

    private TriggerKey triggerKey(Long jobId) {
        return new TriggerKey("trigger-" + jobId, JOB_GROUP);
    }
}
