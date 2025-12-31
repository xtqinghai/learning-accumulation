package com.quinn.hboot.system.controller;

import com.quinn.hboot.common.R;
import com.quinn.hboot.system.model.entity.SysDictItem;
import com.quinn.hboot.system.service.SysDictItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * @author yu-qh
 */
@RestController
@RequestMapping("/api/sys/dict/item")
@RequiredArgsConstructor
public class SysDictItemController {
    private final SysDictItemService itemService;

    @GetMapping("/list/{dictType}")
    public R listByDictType(@PathVariable String dictType) {
        return R.ok(itemService.lambdaQuery()
                .eq(SysDictItem::getDictType, dictType)
                .list());
    }

    @GetMapping("/{id}")
    public R get(@PathVariable Long id) {
        return R.ok(itemService.getById(id));
    }

    @PostMapping
    public R save(@RequestBody SysDictItem entity) {
        entity.setCreateTime(LocalDateTime.now());
        return R.ok(itemService.saveOrUpdate(entity));
    }

    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id) {
        return R.ok(itemService.removeById(id));
    }
}
