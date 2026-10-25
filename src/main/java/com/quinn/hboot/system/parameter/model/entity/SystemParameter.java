package com.quinn.hboot.system.parameter.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_system_parameter")
public class SystemParameter {

    @TableId(type = IdType.AUTO)
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
