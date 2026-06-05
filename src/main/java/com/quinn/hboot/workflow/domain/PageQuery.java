package com.quinn.hboot.workflow.domain;

import com.quinn.hboot.common.base.BasePageQuery;
import lombok.Data;

import java.io.Serial;

@Data
public class PageQuery extends BasePageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

    public PageQuery() {
    }

    public PageQuery(Integer pageSize, Integer pageNum) {
        setPageSize(pageSize);
        setPageNum(pageNum);
    }
}
