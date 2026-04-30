package com.quinn.hboot.system.controller;

import com.quinn.hboot.common.R;
import com.quinn.hboot.system.model.entity.SysMenu;
import com.quinn.hboot.system.service.SysMenuService;
import com.quinn.hboot.utils.TreeBuildUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * 菜单管理接口。
 */
@RestController
@RequestMapping("/api/sys/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService menuService;

    @GetMapping("/list")
    public R list() {
        return R.ok(menuService.listVisibleMenus());
    }

    @GetMapping("/{id}")
    public R get(@PathVariable Long id) {
        return R.ok(menuService.getById(id));
    }

    @PostMapping
    public R add(@RequestBody SysMenu entity) {
        entity.setCreateTime(LocalDateTime.now());
        return R.ok(menuService.save(entity));
    }

    @PutMapping
    public R update(@RequestBody SysMenu entity) {
        entity.setUpdateTime(LocalDateTime.now());
        return R.ok(menuService.updateById(entity));
    }

    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id) {
        return R.ok(menuService.removeById(id));
    }

    @GetMapping("/tree")
    public R tree() {
        return R.ok(TreeBuildUtils.buildTree(
                menuService.listVisibleMenus().stream()
                        .sorted(Comparator.comparing(SysMenu::getSort))
                        .collect(Collectors.toList()),
                SysMenu::getId,
                SysMenu::getParentId,
                SysMenu::setChildren,
                SysMenu::getChildren,
                0L
        ));
    }
}
