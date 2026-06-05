package com.quinn.hboot.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quinn.hboot.system.cache.SystemCacheNames;
import com.quinn.hboot.system.cache.SystemCacheSupport;
import com.quinn.hboot.system.mapper.SysMenuMapper;
import com.quinn.hboot.system.mapper.SysRoleMenuMapper;
import com.quinn.hboot.system.mapper.SysUserRoleMapper;
import com.quinn.hboot.system.model.entity.SysMenu;
import com.quinn.hboot.system.model.entity.SysRoleMenu;
import com.quinn.hboot.system.model.entity.SysUserRole;
import com.quinn.hboot.system.service.SysMenuService;
import com.quinn.hboot.utils.TreeBuildUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    private static final String WORKFLOW_DESIGN_PATH = "/design/index";
    private static final String WORKFLOW_DESIGN_COMPONENT = "workflow/processDefinition/design";

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;
    private final SystemCacheSupport cacheSupport;

    @Override
    @Cacheable(cacheNames = SystemCacheNames.USER_MENU_LIST, key = "#userId", unless = "#result == null")
    public List<SysMenu> findMenusByUserId(Long userId) {
        return filterVisibleMenus(listAssignedMenus(userId));
    }

    @Override
    @Cacheable(cacheNames = SystemCacheNames.USER_PERMISSION_LIST, key = "#userId", unless = "#result == null")
    public List<String> findPermissionsByUserId(Long userId) {
        return listAssignedMenus(userId).stream()
            .map(SysMenu::getPermission)
            .filter(StringUtils::hasText)
            .distinct()
            .toList();
    }

    @Override
    @Cacheable(cacheNames = SystemCacheNames.MENU_VISIBLE_LIST, key = "'all'")
    public List<SysMenu> listVisibleMenus() {
        List<SysMenu> menus = menuMapper.selectList(new QueryWrapper<SysMenu>().orderByAsc("sort"));
        return filterVisibleMenus(menus);
    }

    @Override
    @Cacheable(cacheNames = SystemCacheNames.MENU_VISIBLE_TREE, key = "'tree'")
    public List<SysMenu> listVisibleMenuTree() {
        List<SysMenu> menus = menuMapper.selectList(new QueryWrapper<SysMenu>().orderByAsc("sort"));
        List<SysMenu> visibleMenus = filterVisibleMenus(menus);
        return TreeBuildUtils.buildTree(
            visibleMenus.stream().sorted(Comparator.comparing(SysMenu::getSort)).toList(),
            SysMenu::getId,
            SysMenu::getParentId,
            SysMenu::setChildren,
            SysMenu::getChildren,
            0L
        );
    }

    @Override
    public boolean save(SysMenu entity) {
        boolean result = super.save(entity);
        if (result) {
            refreshMenuCaches();
        }
        return result;
    }

    @Override
    public boolean updateById(SysMenu entity) {
        boolean result = super.updateById(entity);
        if (result) {
            refreshMenuCaches();
        }
        return result;
    }

    @Override
    public boolean removeById(Serializable id) {
        boolean result = super.removeById(id);
        if (result) {
            refreshMenuCaches();
        }
        return result;
    }

    private List<SysMenu> listAssignedMenus(Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(new QueryWrapper<SysUserRole>().eq("user_id", userId));
        if (userRoles == null || userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(new QueryWrapper<SysRoleMenu>().in("role_id", roleIds));
        if (roleMenus == null || roleMenus.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toSet());
        List<SysMenu> menus = menuMapper.selectList(new QueryWrapper<SysMenu>().in("id", menuIds).orderByAsc("sort"));
        return menus == null ? Collections.emptyList() : menus;
    }

    private List<SysMenu> filterVisibleMenus(List<SysMenu> menus) {
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, SysMenu> uniqueMenus = new LinkedHashMap<>();
        menus.stream()
            .filter(menu -> !isWorkflowDesignMenu(menu))
            .sorted(Comparator.comparing(SysMenu::getSort))
            .forEach(menu -> uniqueMenus.putIfAbsent(buildMenuKey(menu), menu));
        return uniqueMenus.values().stream().toList();
    }

    private String buildMenuKey(SysMenu menu) {
        String path = menu.getPath() == null ? "" : menu.getPath().trim();
        String component = menu.getComponent() == null ? "" : menu.getComponent().trim();
        String name = menu.getName() == null ? "" : menu.getName().trim();
        return path + "|" + component + "|" + name + "|" + menu.getType();
    }

    private boolean isWorkflowDesignMenu(SysMenu menu) {
        if (menu == null) {
            return false;
        }
        return WORKFLOW_DESIGN_PATH.equals(menu.getPath())
            || WORKFLOW_DESIGN_COMPONENT.equals(menu.getComponent());
    }

    private void refreshMenuCaches() {
        cacheSupport.clearMenuCaches();
        cacheSupport.clearRoleMenuCaches();
        cacheSupport.clearUserAuthorizationCaches();
    }
}
