package com.quinn.hboot.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.common.R;
import com.quinn.hboot.system.model.entity.SysUser;
import com.quinn.hboot.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author yu-qh
 */
@RestController
@RequestMapping("/api/sys/user")
@RequiredArgsConstructor
public class SysUserController {
    private final SysUserService userService;

    @GetMapping("/page")
    public R page(@RequestParam int pageNum,
                  @RequestParam int pageSize) {
        return R.ok(userService.page(new Page<>(pageNum, pageSize)));
    }

    @GetMapping("/{id}")
    public R get(@PathVariable Long id) {
        return R.ok(userService.getById(id));
    }

    @PostMapping
    public R add(@RequestBody SysUser entity) {
        return R.ok(userService.save(entity));
    }

    @PutMapping
    public R update(@RequestBody SysUser entity) {
        return R.ok(userService.updateById(entity));
    }

    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id) {
        return R.ok(userService.removeById(id));
    }
}
