package com.quinn.hboot.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quinn.hboot.system.cache.SystemCacheNames;
import com.quinn.hboot.system.cache.SystemCacheSupport;
import com.quinn.hboot.system.mapper.SysDeptMapper;
import com.quinn.hboot.system.model.entity.SysDept;
import com.quinn.hboot.system.service.SysDeptService;
import com.quinn.hboot.utils.TreeBuildUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    private final SystemCacheSupport cacheSupport;

    @Override
    @Cacheable(cacheNames = SystemCacheNames.DEPT_LIST, key = "'all'")
    public List<SysDept> list() {
        return super.list();
    }

    @Override
    @Cacheable(cacheNames = SystemCacheNames.DEPT_TREE, key = "'tree'")
    public List<SysDept> listDeptTree() {
        List<SysDept> source = super.list();
        List<SysDept> copied = source.stream().map(this::copyDept).sorted(Comparator.comparing(SysDept::getSort)).toList();
        return TreeBuildUtils.buildTree(
            copied,
            SysDept::getId,
            SysDept::getParentId,
            SysDept::setChildren,
            SysDept::getChildren,
            0L
        );
    }

    @Override
    public List<SysDept> selectDeptList(Wrapper<SysDept> bo) {
        return this.baseMapper.selectList(bo);
    }

    @Override
    public Map<Long, String> selectDeptNamesByIds(List<Long> deptIds) {
        List<SysDept> list = this.baseMapper.selectByIds(deptIds);
        return list.stream().collect(Collectors.toMap(SysDept::getId, SysDept::getName));
    }

    @Override
    public boolean save(SysDept entity) {
        boolean result = super.save(entity);
        if (result) {
            refreshDeptCaches();
        }
        return result;
    }

    @Override
    public boolean updateById(SysDept entity) {
        boolean result = super.updateById(entity);
        if (result) {
            refreshDeptCaches();
        }
        return result;
    }

    @Override
    public boolean removeById(Serializable id) {
        boolean result = super.removeById(id);
        if (result) {
            refreshDeptCaches();
        }
        return result;
    }

    private SysDept copyDept(SysDept source) {
        SysDept target = new SysDept();
        target.setId(source.getId());
        target.setName(source.getName());
        target.setParentId(source.getParentId());
        target.setSort(source.getSort());
        target.setStatus(source.getStatus());
        target.setDeptLeader(source.getDeptLeader());
        target.setCreateTime(source.getCreateTime());
        target.setUpdateTime(source.getUpdateTime());
        return target;
    }

    private void refreshDeptCaches() {
        cacheSupport.clearDeptCaches();
        cacheSupport.clearUserBaseCaches();
    }
}
