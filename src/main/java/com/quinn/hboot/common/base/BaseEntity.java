package com.quinn.hboot.common.base;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @program: h-boot
 * @ClassName BaseEntity
 * @description: 说明：TODO
 * @author: yuqh
 * @create: 2025-12-21 19:07
 * @Version 1.0
 **/
@Data
public class BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
}
