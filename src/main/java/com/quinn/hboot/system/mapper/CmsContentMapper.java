package com.quinn.hboot.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quinn.hboot.system.model.entity.CmsContent;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.system.model.query.CmsContentQuery;
import com.quinn.hboot.system.model.vo.CmsContentVO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统内容管理Mapper接口
 *
 * @author yu-qh
 * @since 2025-12-25 15:37
 */
@Mapper
public interface CmsContentMapper extends BaseMapper<CmsContent> {

    /**
     * 获取系统内容管理分页数据
     *
     * @param page 分页对象
     * @param queryParams 查询参数
     * @return {@link Page<CmsContentVO>} 系统内容管理分页列表
     */
    Page<CmsContentVO> getCmsContentPage(Page<CmsContentVO> page, CmsContentQuery queryParams);

}
