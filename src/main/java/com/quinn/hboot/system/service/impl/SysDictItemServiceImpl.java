package com.quinn.hboot.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quinn.hboot.system.cache.SystemCacheNames;
import com.quinn.hboot.system.cache.SystemCacheSupport;
import com.quinn.hboot.system.mapper.SysDictItemMapper;
import com.quinn.hboot.system.model.entity.SysDictItem;
import com.quinn.hboot.system.service.SysDictItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDictItemServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictItem> implements SysDictItemService {

    private final SystemCacheSupport cacheSupport;

    @Override
    @Cacheable(cacheNames = SystemCacheNames.DICT_ITEMS, key = "#dictType", unless = "#result == null")
    public List<SysDictItem> listByDictType(String dictType) {
        return this.list(new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getDictType, dictType));
    }

    @Override
    public boolean saveOrUpdate(SysDictItem entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            cacheSupport.clearDictCaches();
        }
        return result;
    }

    @Override
    public boolean removeById(Serializable id) {
        boolean result = super.removeById(id);
        if (result) {
            cacheSupport.clearDictCaches();
        }
        return result;
    }
}
