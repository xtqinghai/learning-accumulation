package com.quinn.hboot.system.parameter.model.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SystemParameterForm {

    @NotBlank(message = "参数键不能为空")
    @Pattern(regexp = "[A-Za-z][A-Za-z0-9_.-]{1,99}", message = "参数键只能由字母、数字、点、下划线和短横线组成")
    private String parameterKey;

    @NotBlank(message = "参数名称不能为空")
    @Size(max = 100, message = "参数名称长度不能超过 100 个字符")
    private String parameterName;

    /** Sensitive values are omitted during edit when their existing value should be retained. */
    @Size(max = 4000, message = "参数值长度不能超过 4000 个字符")
    private String parameterValue;

    @NotBlank(message = "参数分类不能为空")
    @Size(max = 50, message = "参数分类长度不能超过 50 个字符")
    private String category;

    @NotNull(message = "敏感标记不能为空")
    @Min(value = 0, message = "敏感标记不合法")
    @Max(value = 1, message = "敏感标记不合法")
    private Integer sensitive;

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态不合法")
    @Max(value = 1, message = "状态不合法")
    private Integer status;

    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;
}
