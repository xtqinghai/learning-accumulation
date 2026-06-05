package com.quinn.hboot.system.model.query;

import com.quinn.hboot.common.base.BasePageQuery;
import lombok.Data;

@Data
public class SysUserPageQuery extends BasePageQuery {

    private String username;
    private Integer status;
}
