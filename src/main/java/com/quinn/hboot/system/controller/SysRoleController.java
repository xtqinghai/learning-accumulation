package com.quinn.hboot.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.common.R;
import com.quinn.hboot.system.model.entity.SysRole;
import com.quinn.hboot.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sys/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @GetMapping("/page")
    public R page(@RequestParam int pageNum,
                  @RequestParam int pageSize) {
        return R.ok(roleService.page(new Page<>(pageNum, pageSize)));
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
}

