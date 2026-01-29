package com.quinn.hboot.system.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yu-qh
 */
@Data
@TableName("sys_dept")
public class SysDept {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long parentId;   // 父部门ID
    private Integer sort;    // 排序
    private Integer status;  // 状态
    private Long deptLeader;  // 部门负责人

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<SysDept> children;
}

