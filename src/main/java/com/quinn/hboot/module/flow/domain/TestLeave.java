package com.quinn.hboot.module.flow.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.quinn.hboot.utils.SecurityUtils;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * OA 请假申请对象 test_leave
 *
 * @author ruoyi
 * @date 2024-03-07
 */
@Data
public class TestLeave
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 请假类型 */
    @ExcelProperty(value = "请假类型")
    private Long type;

    /** 请假原因 */
    private String reason;

    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @ExcelProperty(value = "开始时间")
    private Date startTime;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @ExcelProperty(value = "结束时间")
    private Date endTime;

    /** 请假天数 */
    @ExcelProperty(value = "请假天数")
    private Long day;

    /** 流程实例的id */
    private Long instanceId;

    /** 节点编码 */
    private String nodeCode;

    /** 流程节点名称 */
    @ExcelProperty(value = "流程节点名称")
    private String nodeName;

    /**
     * 节点类型（0开始节点 1中间节点 2结束节点 3互斥网关 4并行网关）
     */
    private Integer nodeType;

    /** 流程状态（0待提交 1审批中 2 审批通过 3自动通过 4终止 5作废 6撤销 7取回  8已完成 9已退回 10失效） */
    @ExcelProperty(value = "流程状态")
    private String flowStatus;

    /**抄送人*/
    private List<String> additionalHandler;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("type", getType())
            .append("reason", getReason())
            .append("startTime", getStartTime())
            .append("endTime", getEndTime())
            .append("day", getDay())
            .append("instanceId", getInstanceId())
            .append("nodeCode", getNodeCode())
            .append("nodeName", getNodeName())
            .append("nodeType", getNodeType())
            .append("flowStatus", getFlowStatus())
            .append("createBy", SecurityUtils.getUserId())
            .append("createTime", getCreateTime())
            .append("updateBy", SecurityUtils.getUserId())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
