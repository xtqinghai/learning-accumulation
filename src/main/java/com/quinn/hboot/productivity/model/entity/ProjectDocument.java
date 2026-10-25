package com.quinn.hboot.productivity.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("project_document")
public class ProjectDocument {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ownerId;
    private Long categoryId;
    private String projectName;
    private String title;
    private String documentType;
    private String content;
    private String tags;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
