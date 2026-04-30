package com.quinn.hboot.workflow.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 任务操作业务对象。
 * <p>
 * 统一承载委派、转办、加签、减签等操作的请求参数。
 *
 * @author AprilWind
 */
@Data
public class TaskOperationBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 委派/转办目标用户 ID。
     */
    private String userId;

    /**
     * 加签/减签目标用户 ID 列表。
     */
    private List<String> userIds;

    /**
     * 任务 ID。
     */
    @NotNull(message = "任务id不能为空")
    private Long taskId;

    /**
     * 审批意见或备注信息。
     */
    private String message;

    /**
     * 消息提醒方式。
     */
    private List<String> messageType;

}
