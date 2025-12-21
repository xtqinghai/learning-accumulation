package com.quinn.hboot.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quinn.hboot.system.model.entity.OperLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.system.model.query.OperLogQuery;
import com.quinn.hboot.system.model.vo.OperLogVO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统操作日志Mapper接口
 *
 * @author yu-qh
 * @since 2025-12-21 19:25
 */
@Mapper
public interface OperLogMapper extends BaseMapper<OperLog> {

    /**
     * 获取系统操作日志分页数据
     *
     * @param page 分页对象
     * @param queryParams 查询参数
     * @return {@link Page<OperLogVO>} 系统操作日志分页列表
     */
    Page<OperLogVO> getOperLogPage(Page<OperLogVO> page, OperLogQuery queryParams);

}
