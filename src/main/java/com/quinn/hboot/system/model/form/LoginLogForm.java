package com.quinn.hboot.system.model.form;

import java.io.Serial;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

/**
 * 系统登录日志表单对象
 *
 * @author yu-qh
 * @since 2025-12-21 18:35
 */
@Getter
@Setter
@Schema(description = "系统登录日志表单对象")
public class LoginLogForm implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户名")
    @NotBlank(message = "用户名不能为空")
    @Size(max=50, message="用户名长度不能超过50个字符")
    private String username;

    @Schema(description = "登录IP")
    @Size(max=50, message="登录IP长度不能超过50个字符")
    private String ip;

    @Schema(description = "IP归属地")
    @Size(max=255, message="IP归属地长度不能超过255个字符")
    private String location;

    @Schema(description = "浏览器")
    @Size(max=100, message="浏览器长度不能超过100个字符")
    private String browser;

    @Schema(description = "操作系统")
    @Size(max=100, message="操作系统长度不能超过100个字符")
    private String os;

    @Schema(description = "0失败 1成功")
    private Integer status;

    @Schema(description = "提示信息")
    @Size(max=255, message="提示信息长度不能超过255个字符")
    private String msg;

    @Schema(description = "登录时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;


}
