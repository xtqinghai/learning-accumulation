package com.quinn.hboot.workflow.domain.bo;

import com.quinn.hboot.workflow.domain.FlowBaseEntity;
import com.quinn.hboot.workflow.domain.FlwForm;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程表单业务对象 flow_form
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = FlwForm.class, reverseConvertGenerate = false)
public class FlwFormBo extends FlowBaseEntity {

    private Long id;

    @NotBlank(message = "表单编码不能为空")
    private String formCode;

    @NotBlank(message = "表单名称不能为空")
    private String formName;

    private String version;

    private Integer isPublish;

    private Integer formType;

    private String formPath;

    private String formContent;

    private String ext;
}
