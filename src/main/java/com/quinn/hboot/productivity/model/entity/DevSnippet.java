package com.quinn.hboot.productivity.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dev_snippet")
public class DevSnippet {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ownerId;
    private Long categoryId;
    private String title;
    private String language;
    private String content;
    private String description;
    private String example;
    private String tags;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
