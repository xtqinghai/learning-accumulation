package com.quinn.hboot.system.model.query;

import com.quinn.hboot.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件管理分页查询对象
 *
 * @author yu-qh
 * @since 2025-12-25 22:55
 */
@Schema(description ="文件管理查询对象")
@Getter
@Setter
public class FileQuery extends BasePageQuery {

    @Schema(description = "原始文件名")
    private String fileName;
}
