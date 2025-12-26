package com.quinn.hboot.system.model.query;

import com.quinn.hboot.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统内容管理分页查询对象
 *
 * @author yu-qh
 * @since 2025-12-25 15:37
 */
@Schema(description ="系统内容管理查询对象")
@Getter
@Setter
public class CmsContentQuery extends BasePageQuery {

    @Schema(description = "内容类型")
    private String type;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "摘要")
    private String summary;
}
