package com.quinn.hboot.system.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yu-qh
 */
@Data
@TableName("sys_role")
public class SysRole {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;     // 角色名称，例如 admin
    private String code;     // 角色标识，例如 ROLE_ADMIN

    private Integer status;      // 1启用 0禁用
    private Integer sort;        // 排序

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

