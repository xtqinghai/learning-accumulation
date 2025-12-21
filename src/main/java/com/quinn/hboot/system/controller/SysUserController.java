package com.quinn.hboot.system.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.common.R;
import com.quinn.hboot.common.SysLog;
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
                  @RequestParam int pageSize, SysUser sysUser) {
        // 2. 构建查询条件（LambdaQueryWrapper，替代直接接收Wrapper）
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        // 按需添加查询条件（非空才拼接，避免查询全部）
        if (sysUser.getUsername() != null && !sysUser.getUsername().isEmpty()) {
            queryWrapper.like(SysUser::getUsername, sysUser.getUsername()); // 用户名模糊查询
        }
        if (sysUser.getStatus() != null) {
            queryWrapper.eq(SysUser::getStatus, sysUser.getStatus()); // 状态精确查询
        }
        return R.ok(userService.page(new Page<>(pageNum, pageSize),queryWrapper));
    }

    @GetMapping("/{id}")
    public R get(@PathVariable Long id) {
        return R.ok(userService.getById(id));
    }

    @SysLog(module = "用户管理", type = "新增用户")
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

    @PutMapping("resetPwd/{id}")
    public R update(@PathVariable Long id) {
        return R.ok(userService.resetPwd(id));
    }
}
