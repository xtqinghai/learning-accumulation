package com.quinn.hboot.flow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quinn.hboot.flow.vo.FlowTaskVo;
import com.quinn.hboot.flow.vo.WarmFlowInteractiveTypeVo;
import com.quinn.hboot.system.model.entity.SysDept;
import com.quinn.hboot.system.model.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.warm.flow.core.entity.HisTask;
import org.dromara.warm.flow.core.entity.Task;
import org.dromara.warm.flow.core.entity.User;
import org.dromara.warm.flow.orm.entity.FlowHisTask;
import org.dromara.warm.flow.orm.entity.FlowTask;

import java.util.List;

/**
 * warm-flow工作流Mapper接口
 *
 * @author ruoyi
 * @date 2024-03-07
 */
@Mapper
public interface WarmFlowMapper  extends BaseMapper<FlowTaskVo> {
    /**
     * 分页查询待办任务
     *
     * @param task 条件实体
     */
    List<FlowTaskVo> toDoPage(@Param("task") Task task);

    /**
     * 获取最新的已办任务
     *
     * @param hisTask
     * @return
     */
    List<FlowHisTask> donePage(@Param("hisTask") HisTask hisTask);

    /**
     * 分页获取抄送任务
     * @param flowTask
     * @return
     */
    List<FlowHisTask> copyPage(@Param("task") FlowTask flowTask);

    /**
     * 根据ID反显姓名
     *
     * @param ids 需要反显姓名的用户ID
     * @return {@link List<User>}
     * @author liangli
     * @date 2024/8/21 17:11
     **/
    List<User> idReverseDisplayName(@Param("ids") Long[] ids);

    /**
     * 查询不为输入的所有用户
     *
     * @param warmFlowInteractiveTypeVo 输入用户编号集合
     * @return 用户列表
     */
    List<User> selectNotUserIds(@Param("warmFlowInteractiveTypeVo") WarmFlowInteractiveTypeVo warmFlowInteractiveTypeVo);

    /**
     * 查询包含输入的所有用户
     *
     * @param warmFlowInteractiveTypeVo 输入用户编号集合
     * @return 用户列表
     */
    List<User> selectUserIds(@Param("warmFlowInteractiveTypeVo")WarmFlowInteractiveTypeVo warmFlowInteractiveTypeVo);

    /**
     * 通过角色ID集合查询角色
     *
     * @param roleIds 角色ID集合
     * @return 角色对象信息
     */
    public List<SysRole> selectRoleByIds(List<Long> roleIds);

    /**
     * 根据部门ID集合查询信息
     *
     * @param deptIds 部门ID集合
     * @return 部门信息
     */
    public List<SysDept> selectDeptByIds(List<Long> deptIds);

    /**
     * 通过用户ID集合查询用户
     *
     * @param userIds 用户ID集合
     * @return 用户对象信息
     */
    List<User> selectUserByIds(List<Long> userIds);

    /**
     * 通过部门id查询部门领导
     *
     * @param deptId 部门id
     * @return 用户对象信息
     */
    List<User> queryLeaderByDeptId(Long deptId);
}
