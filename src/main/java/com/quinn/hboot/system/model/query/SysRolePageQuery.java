package com.quinn.hboot.system.model.query;

import com.quinn.hboot.common.base.BasePageQuery;
import lombok.Data;

@Data
public class SysRolePageQuery extends BasePageQuery {

    private String name;
    private Integer status;
}
