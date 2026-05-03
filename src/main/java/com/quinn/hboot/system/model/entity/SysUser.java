package com.quinn.hboot.system.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yu-qh
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;
    private String nickname;
    private Long deptId;
    private Long roleId;
    private Long postId;
    private Integer status;
    private String email;
    private String mobile;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @Transient
    @TableField(exist = false)
    private String deptName;
    @Transient
    @TableField(exist = false)
    private String roleName;
    @Transient
    @TableField(exist = false)
    private String postName;

    @Transient
    @TableField(exist = false)
    private List<Long> roleIds = new ArrayList<>();

    @Transient
    @TableField(exist = false)
    private List<SysUserOrgPost> orgPosts = new ArrayList<>();
}
