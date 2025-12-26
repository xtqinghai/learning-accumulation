package com.quinn.hboot.system.model.vo;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * 系统内容管理视图对象
 *
 * @author yu-qh
 * @since 2025-12-25 15:37
 */
@Getter
@Setter
@Schema( description = "系统内容管理视图对象")
public class CmsContentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;
    @Schema(description = "内容类型")
    private String type;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "摘要")
    private String summary;
    @Schema(description = "正文（HTML）")
    private String content;
    @Schema(description = "0-草稿 1-发布")
    private Integer status;
    @Schema(description = "排序")
    private Integer sort;
    @Schema(description = "发布时间")
    private LocalDateTime publishTime;
    @Schema(description = "创建人")
    private String creator;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
