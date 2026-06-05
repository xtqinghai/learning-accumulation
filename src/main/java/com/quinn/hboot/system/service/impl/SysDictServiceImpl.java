package com.quinn.hboot.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quinn.hboot.system.cache.SystemCacheNames;
import com.quinn.hboot.system.cache.SystemCacheSupport;
import com.quinn.hboot.system.mapper.SysDictMapper;
import com.quinn.hboot.system.model.entity.SysDict;
import com.quinn.hboot.system.service.SysDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements SysDictService {

    private final SystemCacheSupport cacheSupport;

    @Override
    @Cacheable(cacheNames = SystemCacheNames.DICT_LIST, key = "'all'")
    public List<SysDict> list() {
        return super.list();
    }

    @Override
    public boolean saveOrUpdate(SysDict entity) {
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
