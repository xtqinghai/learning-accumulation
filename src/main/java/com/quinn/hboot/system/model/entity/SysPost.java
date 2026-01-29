package com.quinn.hboot.system.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;
import com.baomidou.mybatisplus.annotation.TableName;
import com.quinn.hboot.common.base.BaseEntity;

import java.time.LocalDateTime;

/**
 * 岗位信息实体对象
 *
 * @author yu-qh
 * @since 2026-01-28 17:27
 */
@Getter
@Setter
@TableName("sys_post")
public class SysPost extends BaseEntity {
    /**
     * 部门id
     */
    private Long deptId;
    /**
     * 岗位编码
     */
    private String postCode;
    /**
     * 岗位类别编码
     */
    private String postCategory;
    /**
     * 岗位名称
     */
    private String postName;
    /**
     * 显示顺序
     */
    private Integer postSort;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 创建部门
     */
    private Long createDept;
    /**
     * 创建者
     */
    private Long createBy;
    /**
     * 更新者
     */
    private Long updateBy;
    /**
     * 备注
     */
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
