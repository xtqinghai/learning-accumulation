package com.quinn.hboot.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quinn.hboot.system.model.entity.SysDept;
import com.quinn.hboot.utils.DataBaseHelper;
import com.quinn.hboot.utils.StreamUtils;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author yu-qh
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {
//    List<Long> selectDeptAndChildById(Long deptId);
    /**
     * 根据父部门ID查询其所有子部门的列表
     *
     * @param parentId 父部门ID
     * @return 包含子部门的列表
     */
    default List<SysDept> selectListByParentId(Long parentId) {
        return this.selectList(new LambdaQueryWrapper<SysDept>()
                .select(SysDept::getId)
                .apply(DataBaseHelper.findInSet(parentId, "parentId")));
    }
    default List<Long> selectDeptAndChildById(Long parentId) {
        List<SysDept> deptList = this.selectListByParentId(parentId);
        List<Long> deptIds = StreamUtils.toList(deptList, SysDept::getId);
        deptIds.add(parentId);
        return deptIds;
    }
}
