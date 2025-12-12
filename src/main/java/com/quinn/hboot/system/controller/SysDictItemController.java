package com.quinn.hboot.system.controller;

import com.quinn.hboot.common.R;
import com.quinn.hboot.system.model.entity.SysDictItem;
import com.quinn.hboot.system.service.SysDictItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author yu-qh
 */
@RestController
@RequestMapping("/api/sys/dict/item")
@RequiredArgsConstructor
public class SysDictItemController {
    private final SysDictItemService itemService;

    @GetMapping("/list/{dictId}")
    public R listByDictId(@PathVariable Long dictId) {
        return R.ok(itemService.lambdaQuery()
                .eq(SysDictItem::getDictId, dictId)
                .list());
    }

    @GetMapping("/{id}")
    public R get(@PathVariable Long id) {
        return R.ok(itemService.getById(id));
    }

    @PostMapping
    public R add(@RequestBody SysDictItem entity) {
        return R.ok(itemService.save(entity));
    }

    @PutMapping
    public R update(@RequestBody SysDictItem entity) {
        return R.ok(itemService.updateById(entity));
    }

    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id) {
        return R.ok(itemService.removeById(id));
    }
}
