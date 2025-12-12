package com.quinn.hboot.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.common.R;
import com.quinn.hboot.system.model.entity.SysRole;
import com.quinn.hboot.system.model.entity.SysUser;
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
    public R page(@RequestParam int pageNum,
                  @RequestParam int pageSize,SysRole sysRole) {
        // 2. 构建查询条件（LambdaQueryWrapper，替代直接接收Wrapper）
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        // 按需添加查询条件（非空才拼接，避免查询全部）
        if (sysRole.getName() != null && !sysRole.getName().isEmpty()) {
            queryWrapper.like(SysRole::getName, sysRole.getName()); // 模糊查询
        }
        return R.ok(roleService.page(new Page<>(pageNum, pageSize),queryWrapper));
    }

    @GetMapping("/{id}")
    public R get(@PathVariable Long id) {
        return R.ok(roleService.getById(id));
    }

    @PostMapping
    public R add(@RequestBody SysRole entity) {
        return R.ok(roleService.save(entity));
    }

    @PutMapping
    public R update(@RequestBody SysRole entity) {
        return R.ok(roleService.updateById(entity));
    }

    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id) {
        return R.ok(roleService.removeById(id));
    }

    @GetMapping("/{id}/menus")
    public R listMenus(@PathVariable Long id) {
        return R.ok(roleService.getRoleMenuIds(id));
    }

    @PostMapping("/{id}/menus")
    public R saveRoleMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        return R.ok(roleService.saveRoleMenus(id,menuIds));
    }
}

