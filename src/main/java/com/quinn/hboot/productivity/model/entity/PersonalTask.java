package com.quinn.hboot.productivity.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("personal_task")
public class PersonalTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ownerId;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Integer progress;
    private LocalDate planDate;
    private LocalDateTime dueTime;
    private String recurrenceRule;
    private LocalDateTime completedTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
