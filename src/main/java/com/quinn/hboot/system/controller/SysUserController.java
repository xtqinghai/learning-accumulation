package com.quinn.hboot.system.controller;

import com.quinn.hboot.common.R;
import com.quinn.hboot.common.log.SysLog;
import com.quinn.hboot.system.model.entity.SysUser;
import com.quinn.hboot.system.model.query.SysUserPageQuery;
import com.quinn.hboot.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sys/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @GetMapping("/page")
    @SysLog(module = "用户管理", type = "分页列表查询")
    public R<?> page(SysUserPageQuery query) {
        SysUser userQuery = new SysUser();
        userQuery.setUsername(query.getUsername());
        userQuery.setStatus(query.getStatus());
        return R.ok(userService.pageAll(query.buildPage(), userQuery));
    }

    @GetMapping("/{id}")
    public R<?> get(@PathVariable Long id) {
        return R.ok(userService.getUserDetail(id));
    }

    @PostMapping
    @SysLog(module = "用户管理", type = "新增")
    public R<?> add(@RequestBody SysUser entity) {
        return R.ok(userService.saveUserRolePost(entity));
    }

    @PutMapping
    @SysLog(module = "用户管理", type = "修改")
    public R<?> update(@RequestBody SysUser entity) {
        return R.ok(userService.updateAllById(entity));
    }

    @DeleteMapping("/{id}")
    @SysLog(module = "用户管理", type = "删除")
    public R<?> delete(@PathVariable Long id) {
        return R.ok(userService.removeById(id));
    }

    @PutMapping("resetPwd/{id}")
    @SysLog(module = "用户管理", type = "重置密码")
    public R<?> resetPwd(@PathVariable Long id) {
        return R.ok(userService.resetPwd(id));
    }
}
