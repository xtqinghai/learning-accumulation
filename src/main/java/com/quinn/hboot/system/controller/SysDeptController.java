package com.quinn.hboot.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.common.R;
import com.quinn.hboot.system.model.entity.SysDept;
import com.quinn.hboot.system.service.SysDeptService;
import com.quinn.hboot.utils.TreeBuildUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * @author yu-qh
 */
@RestController
@RequestMapping("/api/sys/dept")
@RequiredArgsConstructor
public class SysDeptController {
    private final SysDeptService deptService;

    @GetMapping("/list")
    public R list() {
        return R.ok(deptService.list());
    }

    @GetMapping("/{id}")
    public R get(@PathVariable Long id) {
        return R.ok(deptService.getById(id));
    }

    @PostMapping
    public R add(@RequestBody SysDept entity) {
        entity.setCreateTime(LocalDateTime.now());
        return R.ok(deptService.save(entity));
    }

    @PutMapping
    public R update(@RequestBody SysDept entity) {
        entity.setUpdateTime(LocalDateTime.now());
        return R.ok(deptService.updateById(entity));
    }

    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id) {
        return R.ok(deptService.removeById(id));
    }

    @GetMapping("/tree")
    public R tree() {
//        return R.ok(buildTree(menuService.list()));
        return R.ok(TreeBuildUtils.buildTree(
                deptService.list().stream().sorted(Comparator.comparing(SysDept::getSort)).collect(Collectors.toList()),
                SysDept::getId,                // 获取ID
                SysDept::getParentId,          // 获取父ID
                SysDept::setChildren,          // 设置子节点
                SysDept::getChildren,         // 获取子节点
                0L                             // 根节点父ID
        ));
    }
}
