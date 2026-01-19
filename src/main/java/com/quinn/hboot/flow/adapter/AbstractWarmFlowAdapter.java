package com.quinn.hboot.flow.adapter;

import com.quinn.hboot.system.model.entity.User;
import jakarta.annotation.Resource;
import org.dromara.warm.flow.core.enums.CooperateType;
import org.dromara.warm.flow.core.service.TaskService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public abstract class AbstractWarmFlowAdapter {
    @Resource
    protected TaskService taskService;

    /**
     * 获取权限
     *
     * @return 权限列表
     */
    protected List<String> permissionList(User sysUser, String userId) {
        /*List<Role> roles = sysUser.getRoles();
        List<String> permissionList = StreamUtils.toList(roles, role -> "role:" + role.getRoleId());
        permissionList.add(userId);
        permissionList.add("dept:" + sysUser.getDeptId());
        return permissionList;*/
        return null;
    }

    /**
     * 根据类型获取描述
     *
     * @param type 流程类型
     * @return value
     */
    protected String type(Integer type) {
        return CooperateType.getValueByKey(type);
    }
}
