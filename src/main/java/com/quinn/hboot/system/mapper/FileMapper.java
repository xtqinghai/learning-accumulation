package com.quinn.hboot.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quinn.hboot.system.model.entity.SysFile;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.system.model.query.FileQuery;
import com.quinn.hboot.system.model.vo.FileVO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件管理Mapper接口
 *
 * @author yu-qh
 * @since 2025-12-25 22:55
 */
@Mapper
public interface FileMapper extends BaseMapper<SysFile> {

    /**
     * 获取文件管理分页数据
     *
     * @param page 分页对象
     * @param queryParams 查询参数
     * @return {@link Page<FileVO>} 文件管理分页列表
     */
    Page<FileVO> getFilePage(Page<FileVO> page, FileQuery queryParams);

}
