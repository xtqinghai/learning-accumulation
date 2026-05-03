package com.quinn.hboot.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.quinn.hboot.system.model.dto.RegisterUserDTO;
import com.quinn.hboot.system.model.entity.SysUser;
import com.quinn.hboot.system.model.entity.SysUserOrgPost;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SysUserService extends IService<SysUser> {
    void register(RegisterUserDTO dto);

    SysUser getByUsername(String username);

    SysUser getUserDetail(Long id);

    List<String> getUserPermissions(Long userId);

    Set<String> getUserRoleCodes(Long userId);

    List<Long> getUserRoleIds(Long userId);

    List<SysUserOrgPost> getUserOrgPosts(Long userId);

    Object resetPwd(Long id);

    List<SysUser> selectUsersByRoleIds(List<Long> roleIds);

    List<SysUser> selectUsersByDeptIds(List<Long> deptIds);

    List<SysUser> selectUsersByPostIds(List<Long> postIds);

    Map<Long, String> selectUserNamesByIds(List<Long> userIds);

    Object saveUserRolePost(SysUser entity);

    Object updateAllById(SysUser entity);

    IPage<SysUser> pageAll(Page<SysUser> page, SysUser queryWrapper);
}
