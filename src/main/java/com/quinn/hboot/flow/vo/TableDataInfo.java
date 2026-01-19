package com.quinn.hboot.flow.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 表格分页数据对象
 *
 * @author ruoyi
 */
@Data
public class TableDataInfo<E> implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 列表数据 */
    private Data<E> record;

    /** 消息状态码 */
    private int code;

    /** 消息内容 */
    private String msg;

    /**
     * 表格数据对象
     */
    public TableDataInfo()
    {
    }

    /**
     * 分页
     *
     * @param list 列表数据
     * @param total 总记录数
     */
    public TableDataInfo(int code ,String msg,List<E> list, long total)
    {
        this.record = new Data<E>(list,total);
        this.code = code;
        this.msg = msg;
    }

    @lombok.Data
    public static class Data<E> {
        /** 实际数据 */
        private List<E> list;
        /** 总记录数 */
        private long total;
        public Data(List<E> list, long total){
            this.list = list;
            this.total = total;
        }
    }
}
