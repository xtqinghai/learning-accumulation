package com.quinn.hboot.system.parameter.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SystemParameterVO {

    private Long id;
    private String parameterKey;
    private String parameterName;
    private String parameterValue;
    private String category;
    private Integer sensitive;
    private Integer status;
    private String remark;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
}
