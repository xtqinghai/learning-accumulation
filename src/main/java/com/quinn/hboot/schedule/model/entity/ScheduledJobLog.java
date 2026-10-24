package com.quinn.hboot.schedule.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_scheduled_job_log")
public class ScheduledJobLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long jobId;
    private String jobName;
    private String handlerKey;
    private String triggerType;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMs;
    private String message;
    private String errorStack;
}
