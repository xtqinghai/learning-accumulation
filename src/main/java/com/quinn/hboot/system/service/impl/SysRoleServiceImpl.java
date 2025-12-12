package com.quinn.hboot.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quinn.hboot.system.mapper.SysRoleMapper;
import com.quinn.hboot.system.mapper.SysUserMapper;
import com.quinn.hboot.system.model.entity.SysRole;
import com.quinn.hboot.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yu-qh
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole>
        implements SysRoleService {
    private final SysRoleMapper roleMapper;
    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        List<SysRole> menuIds = roleMapper.selectList(new QueryWrapper<SysRole>().eq("user_id", roleId));
        return menuIds.stream().map(SysRole::getId).collect(Collectors.toList());
    }
}

