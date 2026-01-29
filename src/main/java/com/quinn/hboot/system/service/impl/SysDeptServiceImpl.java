package com.quinn.hboot.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quinn.hboot.system.mapper.SysDeptMapper;
import com.quinn.hboot.system.model.entity.SysDept;
import com.quinn.hboot.system.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yu-qh
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept>
        implements SysDeptService {

    @Override
    public List<SysDept> selectDeptList(Wrapper<SysDept> bo) {
        return this.baseMapper.selectList(bo);
    }

    @Override
    public Map<Long, String> selectDeptNamesByIds(List<Long> longIds) {
        List<SysDept> list = this.baseMapper.selectByIds(longIds);
        return list.stream()
            .collect(Collectors.toMap(SysDept::getId, SysDept::getName));
    }
}
