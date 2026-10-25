package com.quinn.hboot.productivity.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_transfer_record")
public class TransferRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ownerId;
    private String direction;
    private String dataType;
    private String format;
    private String status;
    private Integer totalCount;
    private Integer successCount;
    private Integer failedCount;
    private String errorMessage;
    private String payload;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
