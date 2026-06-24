package com.quinn.hboot.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quinn.hboot.system.cache.SystemCacheNames;
import com.quinn.hboot.system.cache.SystemCacheSupport;
import com.quinn.hboot.system.mapper.SysRoleMapper;
import com.quinn.hboot.system.mapper.SysRoleMenuMapper;
import com.quinn.hboot.system.mapper.SysUserMapper;
import com.quinn.hboot.system.mapper.SysUserRoleMapper;
import com.quinn.hboot.system.model.entity.SysRole;
import com.quinn.hboot.system.model.entity.SysRoleMenu;
import com.quinn.hboot.system.model.entity.SysUser;
import com.quinn.hboot.system.model.entity.SysUserRole;
import com.quinn.hboot.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserMapper userMapper;
    private final SystemCacheSupport cacheSupport;

    @Override
    @Cacheable(cacheNames = SystemCacheNames.ROLE_MENU_IDS, key = "#roleId", sync = true)
    public List<Long> getRoleMenuIds(Long roleId) {
        List<SysRoleMenu> menuIds = roleMenuMapper.selectList(new QueryWrapper<SysRoleMenu>().eq("role_id", roleId));
        return menuIds.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveRoleMenus(Long roleId, List<Long> menuIds) {
        if (roleId == null || roleId <= 0) {
            throw new IllegalArgumentException("角色ID不能为空");
        }

        Set<Long> affectedUserIds = findUserIdsByRoleId(roleId);
        roleMenuMapper.deleteByRoleId(roleId);
        if (!CollectionUtils.isEmpty(menuIds)) {
            for (Long menuId : menuIds) {
                if (menuId == null || menuId <= 0) {
                    continue;
                }
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                int insertCount = roleMenuMapper.insert(roleMenu);
                if (insertCount != 1) {
                    throw new IllegalStateException("角色菜单关联保存失败");
                }
            }
        }
        refreshRoleMenuCaches(roleId, affectedUserIds);
        return true;
    }

    @Override
    public List<SysRole> selectRoleList(LambdaQueryWrapper<SysRole> queryWrapper) {
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public Map<Long, String> selectRoleNamesByIds(List<Long> roleIds) {
        List<SysRole> list = this.baseMapper.selectByIds(roleIds);
        return list.stream().collect(Collectors.toMap(SysRole::getId, SysRole::getName));
    }

    @Override
    public boolean save(SysRole entity) {
        boolean result = super.save(entity);
        if (result) {
            refreshRoleCaches(entity == null ? null : entity.getId());
        }
        return result;
    }

    @Override
    public boolean updateById(SysRole entity) {
        Set<Long> affectedUserIds = findUserIdsByRoleId(entity == null ? null : entity.getId());
        boolean result = super.updateById(entity);
        if (result) {
            refreshRoleCaches(entity == null ? null : entity.getId(), affectedUserIds);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        Long roleId = id instanceof Long value ? value : null;
        Set<Long> affectedUserIds = findUserIdsByRoleId(roleId);
        if (roleId != null) {
            roleMenuMapper.deleteByRoleId(roleId);
            userRoleMapper.deleteByRoleId(roleId);
        }
        boolean result = super.removeById(id);
        if (result) {
            refreshRoleCaches(roleId, affectedUserIds);
        }
        return result;
    }

    private void refreshRoleCaches(Long roleId) {
        refreshRoleCaches(roleId, findUserIdsByRoleId(roleId));
    }

    private void refreshRoleCaches(Long roleId, Set<Long> affectedUserIds) {
        cacheSupport.evictRoleMenuCache(roleId);
        if (affectedUserIds == null || affectedUserIds.isEmpty()) {
            return;
        }

        Map<Long, String> usernameMap = userMapper.selectByIds(affectedUserIds).stream()
            .collect(Collectors.toMap(SysUser::getId, SysUser::getUsername, (left, right) -> left));

        affectedUserIds.forEach(userId -> {
            cacheSupport.evictUserBase(userId, Collections.singletonList(usernameMap.get(userId)));
            cacheSupport.evictUserAuthorization(userId);
        });
    }

    private void refreshRoleMenuCaches(Long roleId, Set<Long> affectedUserIds) {
        cacheSupport.evictRoleMenuCache(roleId);
        if (affectedUserIds == null || affectedUserIds.isEmpty()) {
            return;
        }
        affectedUserIds.forEach(cacheSupport::evictUserAuthorization);
    }

    private Set<Long> findUserIdsByRoleId(Long roleId) {
        if (roleId == null) {
            return Collections.emptySet();
        }
        return userRoleMapper.selectList(new QueryWrapper<SysUserRole>().eq("role_id", roleId)).stream()
            .map(SysUserRole::getUserId)
            .filter(userId -> userId != null && userId > 0)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
