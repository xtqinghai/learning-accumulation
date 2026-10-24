package com.quinn.hboot.schedule.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_scheduled_job")
public class ScheduledJob {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String jobName;
    private String handlerKey;
    private String cronExpression;
    private Integer status;
    private String remark;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
}
