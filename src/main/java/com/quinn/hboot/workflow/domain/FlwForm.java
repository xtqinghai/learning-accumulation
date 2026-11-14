package com.quinn.hboot.workflow.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 流程表单对象 flow_form
 */
@Data
@TableName("flow_form")
public class FlwForm implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 表单编码
     */
    private String formCode;

    /**
     * 表单名称
     */
    private String formName;

    /**
     * 表单版本
     */
    private String version;

    /**
     * 是否发布（0未发布 1已发布 9失效）
     */
    private Integer isPublish;

    /**
     * 表单类型（0内置表单 1外挂表单）
     */
    private Integer formType;

    /**
     * 表单路径
     */
    private String formPath;

    /**
     * 表单内容
     */
    private String formContent;

    /**
     * 扩展信息
     */
    private String ext;

    private Date createTime;

    private Date updateTime;

    /**
     * 删除标志
     */
    @TableLogic(value = "0", delval = "1")
    private String delFlag;

    /**
     * 租户id
     */
    private String tenantId;
}
