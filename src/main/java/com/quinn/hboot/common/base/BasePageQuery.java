package com.quinn.hboot.common.base;

import lombok.Data;

/**
 * @program: h-boot
 * @ClassName BasePageQuery
 * @description: 说明：TODO
 * @author: yuqh
 * @create: 2025-12-21 19:00
 * @Version 1.0
 **/
@Data
public class BasePageQuery {
    public int pageNum = 1;
    public int pageSize = 10;
}
