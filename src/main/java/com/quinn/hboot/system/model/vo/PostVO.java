package com.quinn.hboot.system.model.vo;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * 岗位信息视图对象
 *
 * @author yu-qh
 * @since 2026-01-28 17:27
 */
@Getter
@Setter
@Schema( description = "岗位信息视图对象")
public class PostVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "岗位ID")
    private Long id;
    @Schema(description = "部门id")
    private Long deptId;
    @Schema(description = "岗位编码")
    private String postCode;
    @Schema(description = "岗位类别编码")
    private String postCategory;
    @Schema(description = "岗位名称")
    private String postName;
    @Schema(description = "显示顺序")
    private Integer postSort;
    @Schema(description = "状态")
    private Integer status;
    @Schema(description = "创建部门")
    private Long createDept;
    @Schema(description = "创建者")
    private Long createBy;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新者")
    private Long updateBy;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    @Schema(description = "备注")
    private String remark;

    @Schema(description = "部门名称")
    private String deptName;
}
