package com.quinn.hboot.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.quinn.hboot.system.model.entity.SysRole;

import java.util.List;
import java.util.Map;

/**
 * @author yu-qh
 */
public interface SysRoleService extends IService<SysRole> {
    List<Long> getRoleMenuIds(Long roleId);

    Boolean saveRoleMenus(Long id, List<Long> ids);

    List<SysRole> selectRoleList(LambdaQueryWrapper<SysRole> queryWrapper);

    Map<Long, String> selectRoleNamesByIds(List<Long> longIds);
}
