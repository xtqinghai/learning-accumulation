package com.quinn.hboot.system.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * @author yu-qh
 */
@Data
@TableName("sys_user_role")
public class SysUserRole {

//    @TableId(type = IdType.AUTO)
//    private Long id;

    private Long userId;
    private Long roleId;
}

