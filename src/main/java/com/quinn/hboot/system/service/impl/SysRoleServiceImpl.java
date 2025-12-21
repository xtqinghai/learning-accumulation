package com.quinn.hboot.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quinn.hboot.system.mapper.SysRoleMapper;
import com.quinn.hboot.system.mapper.SysRoleMenuMapper;
import com.quinn.hboot.system.model.entity.SysRole;
import com.quinn.hboot.system.model.entity.SysRoleMenu;
import com.quinn.hboot.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yu-qh
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole>
        implements SysRoleService {
//    private final SysRoleMenuService sysRoleMenuService;
    private final SysRoleMenuMapper roleMenuMapper;
    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        List<SysRoleMenu> menuIds = roleMenuMapper.selectList(new QueryWrapper<SysRoleMenu>().eq("role_id", roleId));
        return menuIds.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Boolean saveRoleMenus(Long roleId, List<Long> menuIds) {
        // 1. 参数校验：角色ID不能为空，菜单ID列表为空则仅清空关联
        if (roleId == null || roleId <= 0) {
            throw new IllegalArgumentException("角色ID不能为空");
        }

        // 2. 先清空该角色已关联的所有菜单（避免脏数据）
        roleMenuMapper.deleteByRoleId(roleId);

        // 3. 若菜单ID列表为空，直接返回成功（仅清空）
        if (CollectionUtils.isEmpty(menuIds)) {
            return true;
        }

        // 4. 循环遍历菜单ID，逐个插入（单条保存）
        boolean allSuccess = true; // 标记是否全部插入成功
        for (Long menuId : menuIds) {
            // 过滤无效菜单ID（null/负数）
            if (menuId == null || menuId <= 0) {
                continue;
            }
            // 创建角色-菜单关联对象
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            // 单条插入：mapper.insert()返回受影响行数（1=成功，0=失败）
            int insertCount = roleMenuMapper.insert(roleMenu);
            if (insertCount != 1) { // 插入失败则标记为整体失败
                // 可选：抛出异常触发事务回滚（推荐）
                throw new RuntimeException("菜单ID：" + menuId + " 关联角色失败");
            }
        }

        // 5. 返回最终结果
        return allSuccess;
    }
}

