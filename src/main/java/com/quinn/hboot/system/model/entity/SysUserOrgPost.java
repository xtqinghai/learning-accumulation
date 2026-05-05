package com.quinn.hboot.system.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user_org_post")
public class SysUserOrgPost {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long deptId;
    private Long postId;
    private Integer isPrimary;
    private String jobType;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String deptName;

    @TableField(exist = false)
    private String postName;
}
