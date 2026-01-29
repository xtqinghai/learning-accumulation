package com.quinn.hboot.system.model.query;

import com.quinn.hboot.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 岗位信息分页查询对象
 *
 * @author yu-qh
 * @since 2026-01-28 17:27
 */
@Schema(description ="岗位信息查询对象")
@Getter
@Setter
public class PostQuery extends BasePageQuery {

    @Schema(description = "岗位名称")
    private String postName;

    @Schema(description = "机构ID")
    private Integer deptId;
}
