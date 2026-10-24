package com.quinn.hboot.schedule.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quinn.hboot.schedule.model.entity.ScheduledJob;
import com.quinn.hboot.schedule.model.entity.ScheduledJobLog;
import com.quinn.hboot.schedule.model.form.ScheduledJobForm;
import com.quinn.hboot.schedule.model.query.ScheduledJobLogQuery;
import com.quinn.hboot.schedule.model.query.ScheduledJobQuery;
import com.quinn.hboot.schedule.model.vo.ScheduledJobHandlerVO;

import java.util.List;

public interface ScheduledJobService {

    IPage<ScheduledJob> page(ScheduledJobQuery query);

    IPage<ScheduledJobLog> logPage(ScheduledJobLogQuery query);

    ScheduledJob get(Long id);

    List<ScheduledJobHandlerVO> handlers();

    Long create(ScheduledJobForm form);

    void update(Long id, ScheduledJobForm form);

    void updateStatus(Long id, Integer status);

    void delete(Long id);

    void triggerNow(Long id);

    void schedule(ScheduledJob job);

    void unschedule(Long jobId);

    void restoreActiveJobs();
}
