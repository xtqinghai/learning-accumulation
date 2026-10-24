package com.quinn.hboot.schedule.model.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ScheduledJobForm {

    private Long id;

    @NotBlank(message = "任务名称不能为空")
    @Size(max = 100, message = "任务名称长度不能超过 100 个字符")
    private String jobName;

    @NotBlank(message = "任务处理器不能为空")
    @Size(max = 100, message = "任务处理器长度不能超过 100 个字符")
    private String handlerKey;

    @NotBlank(message = "Cron 表达式不能为空")
    @Size(max = 120, message = "Cron 表达式长度不能超过 120 个字符")
    private String cronExpression;

    @NotNull(message = "任务状态不能为空")
    @Min(value = 0, message = "任务状态不合法")
    @Max(value = 1, message = "任务状态不合法")
    private Integer status;

    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;
}
