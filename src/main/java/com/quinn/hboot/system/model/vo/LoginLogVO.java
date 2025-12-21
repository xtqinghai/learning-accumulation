package com.quinn.hboot.system.model.vo;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * 系统登录日志视图对象
 *
 * @author yu-qh
 * @since 2025-12-21 18:35
 */
@Getter
@Setter
@Schema( description = "系统登录日志视图对象")
public class LoginLogVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户名")
    private String username;
    @Schema(description = "登录IP")
    private String ip;
    @Schema(description = "IP归属地")
    private String location;
    @Schema(description = "浏览器")
    private String browser;
    @Schema(description = "操作系统")
    private String os;
    @Schema(description = "0失败 1成功")
    private Integer status;
    @Schema(description = "提示信息")
    private String msg;
    @Schema(description = "登录时间")
    private LocalDateTime loginTime;
}
