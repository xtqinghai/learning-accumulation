package com.quinn.hboot.system.model.entity;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableName;
import com.quinn.hboot.common.base.BaseEntity;

/**
 * 系统登录日志实体对象
 *
 * @author yu-qh
 * @since 2025-12-21 18:35
 */
@Getter
@Setter
@TableName("sys_login_log")
public class LoginLog {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 用户名
     */
    private String username;
    /**
     * 登录IP
     */
    private String ip;
    /**
     * IP归属地
     */
    private String location;
    /**
     * 浏览器
     */
    private String browser;
    /**
     * 操作系统
     */
    private String os;
    /**
     * 0失败 1成功
     */
    private Integer status;
    /**
     * 提示信息
     */
    private String msg;
    /**
     * 登录时间
     */
    private LocalDateTime loginTime;
}
