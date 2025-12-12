package com.quinn.hboot.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.common.R;
import com.quinn.hboot.system.model.entity.SysDict;
import com.quinn.hboot.system.service.SysDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author yu-qh
 */
@RestController
@RequestMapping("/api/sys/dict")
@RequiredArgsConstructor
public class SysDictController {
    private final SysDictService dictService;

    @GetMapping("/page")
    public R page(@RequestParam int pageNum,
                  @RequestParam int pageSize) {
        return R.ok(dictService.page(new Page<>(pageNum, pageSize)));
    }

    @GetMapping("/{id}")
    public R get(@PathVariable Long id) {
        return R.ok(dictService.getById(id));
    }

    @PostMapping
    public R add(@RequestBody SysDict entity) {
        return R.ok(dictService.save(entity));
    }

    @PutMapping
    public R update(@RequestBody SysDict entity) {
        return R.ok(dictService.updateById(entity));
    }

    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id) {
        return R.ok(dictService.removeById(id));
    }
}
