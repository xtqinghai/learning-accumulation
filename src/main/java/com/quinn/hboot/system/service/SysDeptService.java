package com.quinn.hboot.system.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.quinn.hboot.system.model.entity.SysDept;

import java.util.List;
import java.util.Map;

/**
 * @author yu-qh
 */
public interface SysDeptService extends IService<SysDept> {

    List<SysDept> selectDeptList(Wrapper<SysDept> bo);

    Map<Long, String> selectDeptNamesByIds(List<Long> longIds);

    List<SysDept> listDeptTree();
}
