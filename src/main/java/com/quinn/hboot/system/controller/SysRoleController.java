package com.quinn.hboot.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quinn.hboot.common.R;
import com.quinn.hboot.system.model.entity.SysRole;
import com.quinn.hboot.system.model.query.SysRolePageQuery;
import com.quinn.hboot.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sys/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @GetMapping("/page")
    public R<?> page(SysRolePageQuery query) {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        if (query.getName() != null && !query.getName().isEmpty()) {
            queryWrapper.like(SysRole::getName, query.getName());
        }
        if (query.getStatus() != null) {
            queryWrapper.eq(SysRole::getStatus, query.getStatus());
        }
        queryWrapper.orderByAsc(SysRole::getSort);
        return R.ok(roleService.page(query.buildPage(), queryWrapper));
    }

    @GetMapping("/{id}")
    public R<?> get(@PathVariable Long id) {
        return R.ok(roleService.getById(id));
    }

    @PostMapping
    public R<?> add(@RequestBody SysRole entity) {
        return R.ok(roleService.save(entity));
    }

    @PutMapping
    public R<?> update(@RequestBody SysRole entity) {
        return R.ok(roleService.updateById(entity));
    }

    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        return R.ok(roleService.removeById(id));
    }

    @GetMapping("/{id}/menus")
    public R<?> listMenus(@PathVariable Long id) {
        return R.ok(roleService.getRoleMenuIds(id));
    }

    @PostMapping("/{id}/menus")
    public R<?> saveRoleMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        return R.ok(roleService.saveRoleMenus(id, menuIds));
    }
}
