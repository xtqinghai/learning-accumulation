package com.quinn.hboot.common.base;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quinn.hboot.utils.SqlUtil;
import com.quinn.hboot.utils.StringUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class BasePageQuery implements Serializable {

    public static final int DEFAULT_PAGE_NUM = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;

    private Integer pageNum = DEFAULT_PAGE_NUM;
    private Integer pageSize = DEFAULT_PAGE_SIZE;
    private String orderByColumn;
    private String isAsc;

    public <T> Page<T> buildPage() {
        Integer currentPage = ObjectUtil.defaultIfNull(pageNum, DEFAULT_PAGE_NUM);
        Integer currentPageSize = ObjectUtil.defaultIfNull(pageSize, DEFAULT_PAGE_SIZE);
        if (currentPage <= 0) {
            currentPage = DEFAULT_PAGE_NUM;
        }
        if (currentPageSize <= 0) {
            currentPageSize = DEFAULT_PAGE_SIZE;
        }
        Page<T> page = new Page<>(currentPage, currentPageSize);
        List<OrderItem> orderItems = buildOrderItems();
        if (CollUtil.isNotEmpty(orderItems)) {
            page.addOrder(orderItems);
        }
        return page;
    }

    public <T> Page<T> build() {
        return buildPage();
    }

    @JsonIgnore
    public Integer getFirstNum() {
        return (getPageNum() - 1) * getPageSize();
    }

    private List<OrderItem> buildOrderItems() {
        if (StringUtils.isBlank(orderByColumn) || StringUtils.isBlank(isAsc)) {
            return null;
        }

        String orderBy = StringUtils.toUnderScoreCase(SqlUtil.escapeOrderBySql(orderByColumn));
        String direction = StringUtils.replaceEach(
            isAsc,
            new String[]{"ascending", "descending"},
            new String[]{"asc", "desc"}
        );

        String[] orderByArr = orderBy.split(StringUtils.SEPARATOR);
        String[] isAscArr = direction.split(StringUtils.SEPARATOR);
        if (isAscArr.length != 1 && isAscArr.length != orderByArr.length) {
            throw new IllegalArgumentException("排序参数有误");
        }

        List<OrderItem> result = new ArrayList<>();
        for (int i = 0; i < orderByArr.length; i++) {
            String orderByItem = orderByArr[i];
            String directionItem = isAscArr.length == 1 ? isAscArr[0] : isAscArr[i];
            if ("asc".equals(directionItem)) {
                result.add(OrderItem.asc(orderByItem));
            } else if ("desc".equals(directionItem)) {
                result.add(OrderItem.desc(orderByItem));
            } else {
                throw new IllegalArgumentException("排序参数有误");
            }
        }
        return result;
    }
}
