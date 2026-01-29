package com.quinn.hboot.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quinn.hboot.system.model.entity.SysPost;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.system.model.query.PostQuery;
import com.quinn.hboot.system.model.vo.PostVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 岗位信息Mapper接口
 *
 * @author yu-qh
 * @since 2026-01-28 17:27
 */
@Mapper
public interface SysPostMapper extends BaseMapper<SysPost> {

    /**
     * 获取岗位信息分页数据
     *
     * @param page 分页对象
     * @param queryParams 查询参数
     * @return {@link Page<PostVO>} 岗位信息分页列表
     */
    Page<PostVO> getPostPage(Page<PostVO> page, @Param("queryParams") PostQuery queryParams);

}
