package com.quinn.hboot.system.parameter.model.query;

import com.quinn.hboot.common.base.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SystemParameterQuery extends BasePageQuery {

    private String parameterKey;
    private String category;
    private Integer status;
}
