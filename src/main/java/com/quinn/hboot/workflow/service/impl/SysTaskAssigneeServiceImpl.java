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
//        PageQuery pageQuery = new PageQuery(taskQuery.getPageSize(), taskQuery.getPageNum());
//        SysRole bo = new SysRole();
//        bo.setCode(taskQuery.getHandlerCode());
//        bo.setName(taskQuery.getHandlerName());
//        bo.setStatus(0);
//        Map<String, Object> params = bo.getParams();
//        params.put("beginTime", taskQuery.getBeginTime());
//        params.put("endTime", taskQuery.getEndTime());
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        // 按需添加查询条件（非空才拼接，避免查询全部）
        queryWrapper.eq(SysRole::getCode, taskQuery.getHandlerCode());
        queryWrapper.eq(SysRole::getName, taskQuery.getHandlerName());
        List<SysRole> page = roleService.selectRoleList(queryWrapper);
        // 使用封装的字段映射方法进行转换
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
        PageQuery pageQuery = new PageQuery(taskQuery.getPageSize(), taskQuery.getPageNum());
        SysPost bo = new SysPost();
        bo.setPostCategory(taskQuery.getHandlerCode());
        bo.setPostName(taskQuery.getHandlerName());
//        bo.setStatus("0");
//        Map<String, Object> params = bo.getParams();
//        params.put("beginTime", taskQuery.getBeginTime());
//        params.put("endTime", taskQuery.getEndTime());
//        bo.setBelongDeptId(Convert.toLong(taskQuery.getGroupId()));
        LambdaQueryWrapper<SysPost> queryWrapper = new LambdaQueryWrapper<>(bo);
        queryWrapper.eq(SysPost::getPostCategory, taskQuery.getHandlerCode());
        queryWrapper.eq(SysPost::getPostName, taskQuery.getHandlerName());
        List<SysPost> page = postService.list(queryWrapper);
        // 使用封装的字段映射方法进行转换
        List<TaskAssigneeDTO.TaskHandler> handlers = TaskAssigneeDTO.convertToHandlerList(page,
            item -> Convert.toStr(item.getId()), SysPost::getPostCategory, SysPost::getPostName, item -> Convert.toStr(item.getDeptId()), item -> Date.from(item.getCreateTime().atZone(ZoneId.systemDefault()).toInstant()));
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
        // 非空拼接查询条件
        if (StrUtil.isNotBlank(taskQuery.getHandlerCode())) {
            queryWrapper.eq(SysDept::getId, Convert.toLong(taskQuery.getHandlerCode())); // 部门编码用ID替代，可根据实际调整
        }
        if (StrUtil.isNotBlank(taskQuery.getHandlerName())) {
            queryWrapper.eq(SysDept::getName, taskQuery.getHandlerName());
        }
        queryWrapper.eq(SysDept::getStatus, 0); // 状态正常

        // 分页查询
        Page<SysDept> page = new Page<>(taskQuery.getPageNum(), taskQuery.getPageSize());
        IPage<SysDept> deptPage = deptService.page(page, queryWrapper);
        List<SysDept> deptList = deptPage.getRecords();

        // 显式指定泛型类型SysDept，解决item.getId()爆红问题
        List<TaskAssigneeDTO.TaskHandler> handlers = TaskAssigneeDTO.<SysDept>convertToHandlerList(
                deptList,
                item -> Convert.toStr(item.getId()),          // 唯一标识ID
                item -> Convert.toStr(item.getId()),          // 部门编码（用ID替代，可替换为实际编码字段）
                SysDept::getName,                             // 部门名称
                item -> Convert.toStr(item.getParentId()),    // 父部门ID（分组ID）
                // JDK原生转换（替代Hutool的localDateTimeToDate）
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
        PageQuery pageQuery = new PageQuery(taskQuery.getPageSize(), taskQuery.getPageNum());
        SysUser bo = new SysUser();
        bo.setUsername(taskQuery.getHandlerCode());
        bo.setNickname(taskQuery.getHandlerName());
//        bo.setStatus("0");
//        Map<String, Object> params = bo.getParams();
//        params.put("beginTime", taskQuery.getBeginTime());
//        params.put("endTime", taskQuery.getEndTime());
        bo.setDeptId(Convert.toLong(taskQuery.getGroupId()));

        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();

        Page<SysUser> page = userService.page(new Page<>(taskQuery.getPageNum(), taskQuery.getPageSize()),queryWrapper);
        // 使用封装的字段映射方法进行转换
        List<TaskAssigneeDTO.TaskHandler> handlers = TaskAssigneeDTO.<SysUser>convertToHandlerList(page.getRecords(),
            item -> Convert.toStr(item.getId()), SysUser::getUsername, SysUser::getNickname, item -> Convert.toStr(item.getId()),item -> Date.from(item.getCreateTime().atZone(ZoneId.systemDefault()).toInstant()));
        return new TaskAssigneeDTO(page.getTotal(), handlers);
    }

}
