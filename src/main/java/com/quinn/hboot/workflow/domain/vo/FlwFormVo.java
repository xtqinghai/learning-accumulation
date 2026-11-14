package com.quinn.hboot.workflow.domain.vo;

import com.quinn.hboot.workflow.domain.FlwForm;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 流程表单视图对象 flow_form
 */
@Data
@AutoMapper(target = FlwForm.class)
public class FlwFormVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String formCode;

    private String formName;

    private String version;

    private Integer isPublish;

    private Integer formType;

    private String formPath;

    private String formContent;

    private String ext;

    private Date createTime;

    private Date updateTime;
}
