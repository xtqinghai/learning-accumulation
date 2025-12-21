package com.quinn.hboot.system.model.query;

import com.quinn.hboot.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统操作日志分页查询对象
 *
 * @author yu-qh
 * @since 2025-12-21 19:25
 */
@Schema(description ="系统操作日志查询对象")
@Getter
@Setter
public class OperLogQuery extends BasePageQuery {

    @Schema(description = "模块名称")
    private String module;
    @Schema(description = "方法名")
    private String method;
    @Schema(description = "请求方式")
    private String requestMethod;
}
