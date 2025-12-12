package com.quinn.hboot.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quinn.hboot.system.model.entity.SysRole;

import java.util.List;

/**
 * @author yu-qh
 */
public interface SysRoleService extends IService<SysRole> {
    List<Long> getRoleMenuIds(Long roleId);
}
