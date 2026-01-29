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
 * 岗位信息表单对象
 *
 * @author yu-qh
 * @since 2026-01-28 17:27
 */
@Getter
@Setter
@Schema(description = "岗位信息表单对象")
public class PostForm implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "岗位ID")
    @NotNull(message = "岗位ID不能为空")
    private Long id;

    @Schema(description = "部门id")
    @NotNull(message = "部门id不能为空")
    private Long deptId;

    @Schema(description = "岗位编码")
    @NotBlank(message = "岗位编码不能为空")
    @Size(max=64, message="岗位编码长度不能超过64个字符")
    private String postCode;

    @Schema(description = "岗位类别编码")
    @Size(max=100, message="岗位类别编码长度不能超过100个字符")
    private String postCategory;

    @Schema(description = "岗位名称")
    @NotBlank(message = "岗位名称不能为空")
    @Size(max=50, message="岗位名称长度不能超过50个字符")
    private String postName;

    @Schema(description = "显示顺序")
    @NotNull(message = "显示顺序不能为空")
    private Integer postSort;

    @Schema(description = "状态")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "创建部门")
    private Long createDept;

    @Schema(description = "创建者")
    private Long createBy;

    @Schema(description = "创建时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新者")
    private Long updateBy;

    @Schema(description = "更新时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "备注")
    @Size(max=500, message="备注长度不能超过500个字符")
    private String remark;


}
