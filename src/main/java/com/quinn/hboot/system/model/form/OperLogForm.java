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
 * 系统操作日志表单对象
 *
 * @author yu-qh
 * @since 2025-12-21 19:25
 */
@Getter
@Setter
@Schema(description = "系统操作日志表单对象")
public class OperLogForm implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "模块名称")
    @Size(max=50, message="模块名称长度不能超过50个字符")
    private String module;

    @Schema(description = "操作类型")
    @Size(max=20, message="操作类型长度不能超过20个字符")
    private String type;

    @Schema(description = "方法名")
    @Size(max=255, message="方法名长度不能超过255个字符")
    private String method;

    @Schema(description = "请求方式")
    @Size(max=10, message="请求方式长度不能超过10个字符")
    private String requestMethod;

    @Schema(description = "操作人")
    @Size(max=50, message="操作人长度不能超过50个字符")
    private String operName;

    @Schema(description = "操作IP")
    @Size(max=50, message="操作IP长度不能超过50个字符")
    private String operIp;

    @Schema(description = "请求URL")
    @Size(max=255, message="请求URL长度不能超过255个字符")
    private String operUrl;

    @Schema(description = "请求参数")
    @Size(max=65535, message="请求参数长度不能超过65535个字符")
    private String operParam;

    @Schema(description = "返回结果")
    @Size(max=65535, message="返回结果长度不能超过65535个字符")
    private String jsonResult;

    @Schema(description = "0失败 1成功")
    private Integer status;

    @Schema(description = "错误信息")
    @Size(max=65535, message="错误信息长度不能超过65535个字符")
    private String errorMsg;

    @Schema(description = "操作时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operTime;


}
