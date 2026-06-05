package com.quinn.hboot.system.controller;

import com.quinn.hboot.common.R;
import com.quinn.hboot.system.model.entity.SysDept;
import com.quinn.hboot.system.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/sys/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService deptService;

    @GetMapping("/list")
    public R<?> list() {
        return R.ok(deptService.list());
    }

    @GetMapping("/{id}")
    public R<?> get(@PathVariable Long id) {
        return R.ok(deptService.getById(id));
    }

    @PostMapping
    public R<?> add(@RequestBody SysDept entity) {
        entity.setCreateTime(LocalDateTime.now());
        return R.ok(deptService.save(entity));
    }

    @PutMapping
    public R<?> update(@RequestBody SysDept entity) {
        entity.setUpdateTime(LocalDateTime.now());
        return R.ok(deptService.updateById(entity));
    }

    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        return R.ok(deptService.removeById(id));
    }

    @GetMapping("/tree")
    public R<?> tree() {
        return R.ok(deptService.listDeptTree());
    }
}
