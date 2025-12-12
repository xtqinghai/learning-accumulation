package com.quinn.hboot.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quinn.hboot.system.model.dto.RegisterUserDTO;
import com.quinn.hboot.system.model.entity.SysUser;

import java.util.List;

/**
 * @author yu-qh
 */
public interface SysUserService  extends IService<SysUser> {
    void register(RegisterUserDTO dto);
    SysUser getByUsername(String username);
    List<String> getUserPermissions(Long userId); // 返回 permission 列表

    Object resetPwd(Long id);
//    List<Long> getUserRoleIds(Long userId);
}
