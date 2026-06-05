package com.quinn.hboot.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quinn.hboot.common.R;
import com.quinn.hboot.system.model.entity.SysDict;
import com.quinn.hboot.system.model.entity.SysDictItem;
import com.quinn.hboot.system.service.SysDictItemService;
import com.quinn.hboot.system.service.SysDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/sys/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictService dictService;
    private final SysDictItemService itemService;

    @GetMapping("/list")
    public R<?> list() {
        return R.ok(dictService.list());
    }

    @GetMapping("/{id}")
    public R<?> get(@PathVariable Long id) {
        return R.ok(dictService.getById(id));
    }

    @PostMapping
    public R<?> save(@RequestBody SysDict entity) {
        entity.setCreateTime(LocalDateTime.now());
        return R.ok(dictService.saveOrUpdate(entity));
    }

    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        SysDict dict = dictService.getById(id);
        if (dict != null) {
            itemService.remove(new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getDictType, dict.getDictType()));
        }
        return R.ok(dictService.removeById(id));
    }
}
