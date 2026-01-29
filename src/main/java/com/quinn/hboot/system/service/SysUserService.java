package com.quinn.hboot.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.quinn.hboot.system.model.dto.RegisterUserDTO;
import com.quinn.hboot.system.model.entity.SysUser;

import java.util.List;
import java.util.Map;

/**
 * @author yu-qh
 */
public interface SysUserService  extends IService<SysUser> {
    void register(RegisterUserDTO dto);
    SysUser getByUsername(String username);
    List<String> getUserPermissions(Long userId); // 返回 permission 列表

    Object resetPwd(Long id);

    List<SysUser> selectUsersByRoleIds(List<Long> longIds);

    List<SysUser> selectUsersByDeptIds(List<Long> longIds);

    List<SysUser> selectUsersByPostIds(List<Long> longIds);

    Map<Long, String> selectUserNamesByIds(List<Long> longIds);

    Object saveUserRolePost(SysUser entity);

    Object updateAllById(SysUser entity);

    IPage<SysUser> pageAll(Page<SysUser> objectPage, SysUser queryWrapper);
//    List<Long> getUserRoleIds(Long userId);
}
