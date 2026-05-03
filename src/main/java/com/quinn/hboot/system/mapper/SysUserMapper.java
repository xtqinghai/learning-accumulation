package com.quinn.hboot.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.system.model.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author yu-qh
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    List<SysUser> selectUsersByRoleIds(List<Long> longIds);

    List<SysUser> selectUsersByDeptIds(List<Long> longIds);

    List<SysUser> selectUsersByPostIds(List<Long> longIds);

    Page<SysUser> selectUserPage(Page<SysUser> objectPage, @Param("queryParams") SysUser queryParams);

    SysUser selectUserDetail(@Param("id") Long id);
}
