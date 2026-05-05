package com.quinn.hboot.workflow.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.system.model.entity.SysDept;
import com.quinn.hboot.system.model.entity.SysPost;
import com.quinn.hboot.system.model.entity.SysRole;
import com.quinn.hboot.system.model.entity.SysUser;
import com.quinn.hboot.system.service.SysDeptService;
import com.quinn.hboot.system.service.SysPostService;
import com.quinn.hboot.system.service.SysRoleService;
import com.quinn.hboot.system.service.SysUserService;
import com.quinn.hboot.workflow.domain.PageQuery;
import com.quinn.hboot.workflow.domain.TaskAssigneeBody;
import com.quinn.hboot.workflow.domain.dto.TaskAssigneeDTO;
import com.quinn.hboot.workflow.service.TaskAssigneeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * 工作流设计器获取任务执行人
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysTaskAssigneeServiceImpl implements TaskAssigneeService {

    // 上级Service注入下级Service 其他Service永远不可能注入当前类 避免循环注入
    private final SysPostService postService;
    private final SysDeptService deptService;
    private final SysUserService userService;
    private final SysRoleService roleService;

    /**
     * 查询角色并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    @Override
    public TaskAssigneeDTO selectRolesByTaskAssigneeList(TaskAssigneeBody taskQuery) {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(taskQuery.getHandlerCode()), SysRole::getCode, taskQuery.getHandlerCode());
        queryWrapper.like(StrUtil.isNotBlank(taskQuery.getHandlerName()), SysRole::getName, taskQuery.getHandlerName());
        List<SysRole> page = roleService.selectRoleList(queryWrapper);
        List<TaskAssigneeDTO.TaskHandler> handlers = TaskAssigneeDTO.convertToHandlerList(page,
            item -> Convert.toStr(item.getId()), SysRole::getCode, SysRole::getName, item -> "", item -> Date.from(item.getCreateTime().atZone(ZoneId.systemDefault()).toInstant()));
        return new TaskAssigneeDTO((long) page.size(), handlers);
    }

    /**
     * 查询岗位并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    @Override
    public TaskAssigneeDTO selectPostsByTaskAssigneeList(TaskAssigneeBody taskQuery) {
        LambdaQueryWrapper<SysPost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(taskQuery.getHandlerCode()), SysPost::getPostCode, taskQuery.getHandlerCode());
        queryWrapper.like(StrUtil.isNotBlank(taskQuery.getHandlerName()), SysPost::getPostName, taskQuery.getHandlerName());
        queryWrapper.eq(StrUtil.isNotBlank(taskQuery.getGroupId()), SysPost::getDeptId, Convert.toLong(taskQuery.getGroupId()));
        queryWrapper.eq(SysPost::getStatus, 1);
        List<SysPost> page = postService.list(queryWrapper);
        List<TaskAssigneeDTO.TaskHandler> handlers = TaskAssigneeDTO.convertToHandlerList(page,
            item -> Convert.toStr(item.getId()), SysPost::getPostCode, SysPost::getPostName, item -> Convert.toStr(item.getDeptId()), item -> Date.from(item.getCreateTime().atZone(ZoneId.systemDefault()).toInstant()));
        return new TaskAssigneeDTO((long) page.size(), handlers);
    }

    /**
     * 查询部门并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    @Override
    public TaskAssigneeDTO selectDeptsByTaskAssigneeList(TaskAssigneeBody taskQuery) {
        LambdaQueryWrapper<SysDept> queryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(taskQuery.getHandlerCode())) {
            queryWrapper.eq(SysDept::getId, Convert.toLong(taskQuery.getHandlerCode()));
        }
        if (StrUtil.isNotBlank(taskQuery.getHandlerName())) {
            queryWrapper.like(SysDept::getName, taskQuery.getHandlerName());
        }
        queryWrapper.eq(SysDept::getStatus, 1);

        Page<SysDept> page = new Page<>(taskQuery.getPageNum(), taskQuery.getPageSize());
        IPage<SysDept> deptPage = deptService.page(page, queryWrapper);
        List<SysDept> deptList = deptPage.getRecords();

        List<TaskAssigneeDTO.TaskHandler> handlers = TaskAssigneeDTO.<SysDept>convertToHandlerList(
                deptList,
                item -> Convert.toStr(item.getId()),
                item -> Convert.toStr(item.getId()),
                SysDept::getName,
                item -> Convert.toStr(item.getParentId()),
                item -> Date.from(item.getCreateTime().atZone(ZoneId.systemDefault()).toInstant())
        );
        return new TaskAssigneeDTO(deptPage.getTotal(), handlers);
    }

    /**
     * 查询用户并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    @Override
    public TaskAssigneeDTO selectUsersByTaskAssigneeList(TaskAssigneeBody taskQuery) {
        SysUser queryUser = new SysUser();
        queryUser.setUsername(taskQuery.getHandlerCode());
        queryUser.setNickname(taskQuery.getHandlerName());
        if (StrUtil.isNotBlank(taskQuery.getGroupId())) {
            queryUser.setDeptId(Convert.toLong(taskQuery.getGroupId()));
        }

        // Reuse the system user query so workflow assignee search stays aligned with
        // the current org-post model and doesn't depend on a single hard-coded status value.
        Page<SysUser> page = (Page<SysUser>) userService.pageAll(
            new Page<>(taskQuery.getPageNum(), taskQuery.getPageSize()),
            queryUser
        );
        List<TaskAssigneeDTO.TaskHandler> handlers = TaskAssigneeDTO.<SysUser>convertToHandlerList(page.getRecords(),
            item -> Convert.toStr(item.getId()), SysUser::getUsername, SysUser::getNickname, item -> Convert.toStr(item.getDeptId()), item -> Date.from(item.getCreateTime().atZone(ZoneId.systemDefault()).toInstant()));
        return new TaskAssigneeDTO(page.getTotal(), handlers);
    }

}
