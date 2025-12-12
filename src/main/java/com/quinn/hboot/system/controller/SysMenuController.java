package com.quinn.hboot.system.controller;

import com.quinn.hboot.common.R;
import com.quinn.hboot.system.model.entity.SysMenu;
import com.quinn.hboot.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}
