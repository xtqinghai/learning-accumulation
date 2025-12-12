package com.quinn.hboot.system.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yu-qh
 */
@Data
@TableName("sys_menu")
public class SysMenu {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentId;     // 上级菜单
    private String name;       // 菜单名称
    private String path;
    @TableField("component")// 路由路径
    private String component;  // Vue 组件路径
    private String icon;       // 图标
    private Integer type;      // 0目录 1菜单 2按钮权限
    private String permission; // 按钮权限标识，例如 user:add

    private Integer sort;
    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<SysMenu> children;
}

