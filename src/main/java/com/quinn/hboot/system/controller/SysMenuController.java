package com.quinn.hboot.system.controller;

import com.quinn.hboot.common.R;
import com.quinn.hboot.system.model.entity.SysMenu;
import com.quinn.hboot.system.service.SysMenuService;
import com.quinn.hboot.utils.TreeBuildUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yu-qh
 */
@RestController
@RequestMapping("/api/sys/menu")
@RequiredArgsConstructor
public class SysMenuController {
    private final SysMenuService menuService;

    @GetMapping("/list")
    public R list() {
        return R.ok(menuService.list());
    }

    @GetMapping("/{id}")
    public R get(@PathVariable Long id) {
        return R.ok(menuService.getById(id));
    }

    @PostMapping
    public R add(@RequestBody SysMenu entity) {
        return R.ok(menuService.save(entity));
    }

    @PutMapping
    public R update(@RequestBody SysMenu entity) {
        return R.ok(menuService.updateById(entity));
    }

    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id) {
        return R.ok(menuService.removeById(id));
    }

    @GetMapping("/tree")
    public R tree() {
//        return R.ok(buildTree(menuService.list()));
        return R.ok(TreeBuildUtils.buildTree(
                menuService.list(),
                SysMenu::getId,                // 获取ID
                SysMenu::getParentId,          // 获取父ID
                SysMenu::setChildren,          // 设置子节点
                SysMenu::getChildren,         // 获取子节点
                0L                             // 根节点父ID
        ));
    }

    // 后端工具方法：扁平菜单列表转树形
    /*public List<SysMenu> buildTree(List<SysMenu> menuList) {
        // 1. 构建ID->菜单的映射，方便快速查找
        Map<Long, SysMenu> menuMap = menuList.stream()
                .collect(Collectors.toMap(SysMenu::getId, menu -> menu));
        // 2. 组装树形
        List<SysMenu> tree = new ArrayList<>();
        for (SysMenu menu : menuList) {
            if (menu.getParentId() == 0) {
                tree.add(menu); // 根节点直接加入
            } else {
                // 找到父节点，把当前菜单加入父节点的children
                SysMenu parent = menuMap.get(menu.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(menu);
                }
            }
        }
        return tree;
    }*/
}
