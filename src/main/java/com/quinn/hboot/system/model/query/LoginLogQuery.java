package com.quinn.hboot.system.model.query;

import com.quinn.hboot.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统登录日志分页查询对象
 *
 * @author yu-qh
 * @since 2025-12-21 18:35
 */
@Schema(description ="系统登录日志查询对象")
@Getter
@Setter
public class LoginLogQuery extends BasePageQuery {

    @Schema(description = "用户名")
    private String username;
}
