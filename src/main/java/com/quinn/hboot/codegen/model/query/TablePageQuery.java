package com.quinn.hboot.codegen.model.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quinn.hboot.common.base.BasePageQuery;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.List;

@Getter
@Setter
public class TablePageQuery extends BasePageQuery {

    private String keywords;

    @JsonIgnore
    private List<String> excludeTables;

    @Serial
    private static final long serialVersionUID = 1L;
}
