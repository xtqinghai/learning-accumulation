package com.quinn.hboot.system.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * @author yu-qh
 */
@Data
@TableName("sys_role_menu")
public class SysRoleMenu {

//    @TableId(type = IdType.AUTO)
//    private Long id;

    private Long roleId;
    private Long menuId;
}

