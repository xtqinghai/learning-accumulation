package com.quinn.hboot.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.common.R;
import com.quinn.hboot.system.model.entity.SysDict;
import com.quinn.hboot.system.model.entity.SysDictItem;
import com.quinn.hboot.system.service.SysDictItemService;
import com.quinn.hboot.system.service.SysDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yu-qh
 */
@RestController
@RequestMapping("/api/sys/dict")
@RequiredArgsConstructor
public class SysDictController {
    private final SysDictService dictService;
    private final SysDictItemService itemService;
    @GetMapping("/list")
    public R list() {
        return R.ok(dictService.list());
    }

    @GetMapping("/{id}")
    public R get(@PathVariable Long id) {
        return R.ok(dictService.getById(id));
    }

    @PostMapping
    public R save(@RequestBody SysDict entity) {
        entity.setCreateTime(LocalDateTime.now());
        return R.ok(dictService.saveOrUpdate(entity));
    }

    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id) {
        itemService.removeByIds(itemService.lambdaQuery()
                .eq(SysDictItem::getDictType, dictService.getById(id).getDictType())
                .list());
        return R.ok(dictService.removeById(id));
    }
}
