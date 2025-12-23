package com.quinn.hboot.system.model.vo;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * 系统操作日志视图对象
 *
 * @author yu-qh
 * @since 2025-12-21 19:25
 */
@Getter
@Setter
@Schema( description = "系统操作日志视图对象")
public class OperLogVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "模块名称")
    private String module;
    @Schema(description = "操作类型")
    private String type;
    @Schema(description = "方法名")
    private String method;
    @Schema(description = "请求方式")
    private String requestMethod;
    @Schema(description = "操作人")
    private String operName;
    @Schema(description = "操作IP")
    private String operIp;
    @Schema(description = "请求URL")
    private String operUrl;
    @Schema(description = "请求参数")
    private String operParam;
    @Schema(description = "返回结果")
    private String jsonResult;
    @Schema(description = "0失败 1成功")
    private Integer status;
    @Schema(description = "错误信息")
    private String errorMsg;
    @Schema(description = "操作时间")
    private LocalDateTime operTime;
    @Schema(description = "执行时间")
    private Long executionTime;
}
