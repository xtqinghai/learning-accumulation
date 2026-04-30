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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单服务实现。
 * 流程设计页保留为内部隐藏路由，只允许从流程定义页面进入，
 * 因此这里统一过滤掉对应菜单项，避免在左侧菜单和菜单管理树中单独展示。
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    private static final String WORKFLOW_DESIGN_PATH = "/design/index";
    private static final String WORKFLOW_DESIGN_COMPONENT = "workflow/processDefinition/design";

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;

    @Override
    public List<SysMenu> findMenusByUserId(Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new QueryWrapper<SysUserRole>().eq("user_id", userId)
        );
        if (userRoles == null || userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toSet());

        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(
                new QueryWrapper<SysRoleMenu>().in("role_id", roleIds)
        );
        if (roleMenus == null || roleMenus.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> menuIds = roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toSet());

        List<SysMenu> menus = menuMapper.selectList(
                new QueryWrapper<SysMenu>().in("id", menuIds).orderByAsc("sort")
        );
        return filterVisibleMenus(menus);
    }

    @Override
    public List<SysMenu> listVisibleMenus() {
        List<SysMenu> menus = menuMapper.selectList(new QueryWrapper<SysMenu>().orderByAsc("sort"));
        return filterVisibleMenus(menus);
    }

    private List<SysMenu> filterVisibleMenus(List<SysMenu> menus) {
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }

        return menus.stream()
                .filter(menu -> !isWorkflowDesignMenu(menu))
                .sorted(Comparator.comparing(SysMenu::getSort))
                .collect(Collectors.toList());
    }

    private boolean isWorkflowDesignMenu(SysMenu menu) {
        if (menu == null) {
            return false;
        }
        return WORKFLOW_DESIGN_PATH.equals(menu.getPath())
                || WORKFLOW_DESIGN_COMPONENT.equals(menu.getComponent());
    }
}
