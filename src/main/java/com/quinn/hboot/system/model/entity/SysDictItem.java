package com.quinn.hboot.system.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yu-qh
 */
@Data
@TableName("sys_dict_item")
public class SysDictItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String dictType;
    private String label;
    private String value;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
