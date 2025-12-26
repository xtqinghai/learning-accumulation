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
 * 系统内容管理表单对象
 *
 * @author yu-qh
 * @since 2025-12-25 15:37
 */
@Getter
@Setter
@Schema(description = "系统内容管理表单对象")
public class CmsContentForm implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "内容类型")
    @NotBlank(message = "内容类型不能为空")
    @Size(max=50, message="内容类型长度不能超过50个字符")
    private String type;

    @Schema(description = "标题")
    @NotBlank(message = "标题不能为空")
    @Size(max=200, message="标题长度不能超过200个字符")
    private String title;

    @Schema(description = "摘要")
    @NotBlank(message = "摘要不能为空")
    @Size(max=500, message="摘要长度不能超过500个字符")
    private String summary;

    @Schema(description = "正文（HTML）")
    @Size(max=-1, message="正文（HTML）长度不能超过-1个字符")
    private String content;

    @Schema(description = "0-草稿 1-发布")
    @NotNull(message = "0-草稿 1-发布不能为空")
    private Integer status;

    @Schema(description = "排序")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "发布时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    @Schema(description = "创建人")
    @NotNull(message = "创建人不能为空")
    private String creator;

    @Schema(description = "创建时间")
    @NotNull(message = "创建时间不能为空")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @NotNull(message = "更新时间不能为空")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;


}
