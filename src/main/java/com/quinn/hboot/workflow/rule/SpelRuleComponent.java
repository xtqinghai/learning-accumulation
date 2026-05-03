package com.quinn.hboot.workflow.rule;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quinn.hboot.system.model.entity.SysPost;
import com.quinn.hboot.system.model.entity.SysRole;
import com.quinn.hboot.system.model.entity.SysUser;
import com.quinn.hboot.system.service.SysDeptService;
import com.quinn.hboot.system.service.SysPostService;
import com.quinn.hboot.system.service.SysRoleService;
import com.quinn.hboot.system.service.SysUserService;
import com.quinn.hboot.workflow.common.ConditionalOnEnable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.dromara.common.core.exception.ServiceException;
//import org.dromara.common.core.service.DeptService;
//import org.dromara.workflow.common.ConditionalOnEnable;
import org.springframework.stereotype.Component;

/**
 * spel表达式规则组件
 * <p>
 *     通过该组件统一管理流程定义中的spel表达式
 * </p>
 *
 * @author Michelle.Chung
 */
@ConditionalOnEnable
@Slf4j
@Component
@RequiredArgsConstructor
public class SpelRuleComponent {

    private final SysDeptService deptService;
    private final SysUserService userService;
    private final SysRoleService roleService;
    private final SysPostService postService;

    /**
     * 通过发起人部门id获取部门负责人
     */
    public Long selectDeptLeaderById(Long initiatorDeptId) {
        Long leaderId = deptService.getById(initiatorDeptId).getDeptLeader();
        if (ObjectUtil.isNull(leaderId)) {
            throw new RuntimeException("当前部门未设置负责人，请联系管理员操作。");
        }
        return leaderId;
    }

    /**
     * 通过角色编码获取审批人。
     */
    public Long selectUserByRoleCode(String roleCode) {
        if (ObjectUtil.isEmpty(roleCode)) {
            throw new RuntimeException("角色编码不能为空。");
        }
        SysRole role = roleService.getOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, roleCode).last("limit 1"));
        if (role == null) {
            throw new RuntimeException("未找到角色编码为 " + roleCode + " 的角色配置。");
        }
        return userService.selectUsersByRoleIds(java.util.List.of(role.getId())).stream()
            .filter(user -> ObjectUtil.equal(user.getStatus(), 1))
            .map(SysUser::getId)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("角色 " + roleCode + " 未分配可用用户。"));
    }

    /**
     * 通过岗位编码获取审批人。
     */
    public Long selectUserByPostCode(String postCode) {
        if (ObjectUtil.isEmpty(postCode)) {
            throw new RuntimeException("岗位编码不能为空。");
        }
        SysPost post = postService.getOne(new LambdaQueryWrapper<SysPost>().eq(SysPost::getPostCode, postCode).last("limit 1"));
        if (post == null) {
            throw new RuntimeException("未找到岗位编码为 " + postCode + " 的岗位配置。");
        }
        return userService.selectUsersByPostIds(java.util.List.of(post.getId())).stream()
            .filter(user -> ObjectUtil.equal(user.getStatus(), 1))
            .map(SysUser::getId)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("岗位 " + postCode + " 未分配可用用户。"));
    }

    public Boolean leaveDaysGreaterThan3(Integer leaveDays) {
        return leaveDays != null && leaveDays > 3;
    }

    public Boolean leaveDaysLessOrEqual3(Integer leaveDays) {
        return leaveDays != null && leaveDays <= 3;
    }
}
