package com.quinn.hboot.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quinn.hboot.system.mapper.SysMenuMapper;
import com.quinn.hboot.system.mapper.SysRoleMenuMapper;
import com.quinn.hboot.system.mapper.SysUserRoleMapper;
import com.quinn.hboot.system.model.entity.SysMenu;
import com.quinn.hboot.system.model.entity.SysRoleMenu;
import com.quinn.hboot.system.model.entity.SysUserRole;
import com.quinn.hboot.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yu-qh
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu>
        implements SysMenuService {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;

    /**
     * 根据用户 id 查菜单（通过角色关联）
     */
    @Override
    public List<SysMenu> findMenusByUserId(Long userId) {
        // 1. 查询用户角色
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new QueryWrapper<SysUserRole>().eq("user_id", userId)
        );
        if (userRoles == null || userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());

        // 2. 根据角色查询 menuId
        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(
                new QueryWrapper<SysRoleMenu>().in("role_id", roleIds)
        );
        if (roleMenus == null || roleMenus.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toSet());

        // 3. 根据 menuIds 查询菜单实体（并按 sort 排序）
        List<SysMenu> menus = menuMapper.selectList(
                new QueryWrapper<SysMenu>().in("id", menuIds).orderByAsc("sort")
        );
        return menus;
    }

    /*@Override
    public List<SysMenu> listAllMenus() {
        return menuMapper.selectList(new QueryWrapper<SysMenu>().orderByAsc("sort"));
    }*/
}
