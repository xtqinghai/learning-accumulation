package com.quinn.hboot.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quinn.hboot.system.model.entity.LoginLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.system.model.query.LoginLogQuery;
import com.quinn.hboot.system.model.vo.LoginLogVO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统登录日志Mapper接口
 *
 * @author yu-qh
 * @since 2025-12-21 18:35
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {

    /**
     * 获取系统登录日志分页数据
     *
     * @param page 分页对象
     * @param queryParams 查询参数
     * @return {@link Page<LoginLogVO>} 系统登录日志分页列表
     */
    Page<LoginLogVO> getLoginLogPage(Page<LoginLogVO> page, LoginLogQuery queryParams);

}
