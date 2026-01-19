package com.quinn.hboot.module.flow.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.quinn.hboot.flow.mapper.WarmFlowMapper;
import com.quinn.hboot.system.mapper.SysDeptMapper;
import com.quinn.hboot.system.mapper.SysRoleMapper;
import com.quinn.hboot.system.mapper.SysUserMapper;
import com.quinn.hboot.system.model.entity.SysDept;
import com.quinn.hboot.system.model.entity.SysRole;
import com.quinn.hboot.system.model.entity.SysUser;
import jakarta.annotation.Resource;
import org.dromara.warm.flow.core.entity.User;
import org.dromara.warm.flow.core.utils.CollUtil;
import org.dromara.warm.flow.core.utils.MapUtil;
import org.dromara.warm.flow.core.utils.MathUtil;
import org.dromara.warm.flow.core.utils.StreamUtils;
import org.dromara.warm.flow.ui.dto.HandlerFunDto;
import org.dromara.warm.flow.ui.dto.HandlerQuery;
import org.dromara.warm.flow.ui.dto.TreeFunDto;
import org.dromara.warm.flow.ui.service.HandlerSelectService;
import org.dromara.warm.flow.ui.vo.HandlerFeedBackVo;
import org.dromara.warm.flow.ui.vo.HandlerSelectVo;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 流程设计器-获取办理人权限设置列表接口实现类
 *
 * @author warm
 */
@Component
public class HandlerSelectServiceImpl implements HandlerSelectService {

    @Resource
    private SysUserMapper userMapper;

    @Resource
    private SysRoleMapper roleMapper;

    @Resource
    private SysDeptMapper deptMapper;

    @Resource
    private WarmFlowMapper warmFlowMapper;

    /**
     * 获取办理人权限设置列表tabs页签，如：用户、角色和部门等，可以返回其中一种或者多种，按业务需求决定
     * @return tabs页签
     */
    @Override
    public List<String> getHandlerType() {
        return Arrays.asList("用户", "角色", "部门");
    }

    /**
     * 获取用户列表、角色列表、部门列表等，可以返回其中一种或者多种，按业务需求决定
     * @param query 查询参数
     * @return 结果
     */
    @Override
    public HandlerSelectVo getHandlerSelect(HandlerQuery query) {

        if ("角色".equals(query.getHandlerType())) {
            return getRole(query);
        }

        if ("部门".equals(query.getHandlerType())) {
            return getDept(query);
        }

        if ("用户".equals(query.getHandlerType())) {
            return getUser(query);
        }

        return new HandlerSelectVo();
    }

    @Override
    public List<HandlerFeedBackVo> handlerFeedback(List<String> storageIds) {
        List<HandlerFeedBackVo> handlerFeedBackVos = new ArrayList<>();
        if (CollUtil.isNotEmpty(storageIds)) {
            List<Long> roleIdList = new ArrayList<>();
            List<Long> deptIdList = new ArrayList<>();
            List<Long> userIdList = new ArrayList<>();

            // 分别过滤出用户、角色和部门的id，分别用集合存储
            for (String storageId : storageIds) {
                if (storageId.startsWith("role:")) {
                    roleIdList.add(Long.valueOf(storageId.replace("role:", "")));
                } else if (storageId.startsWith("dept:")) {
                    deptIdList.add(Long.valueOf(storageId.replace("dept:", "")));
                } else if (MathUtil.isNumeric(storageId)){
                    userIdList.add(Long.valueOf(storageId));
                }
            }

            Map<String, String> authMap = new HashMap<>();
            // 查询角色id对应的名称
            if (CollUtil.isNotEmpty(roleIdList)) {
                // 查询角色列表
                List<SysRole> roleList = warmFlowMapper.selectRoleByIds(roleIdList);
                authMap.putAll(StreamUtils.toMap(roleList, role -> "role:" + role.getId()
                        , SysRole::getName));
            }

            // 查询部门id对应的名称
            if (CollUtil.isNotEmpty(deptIdList)) {
                List<SysDept> deptList = warmFlowMapper.selectDeptByIds(deptIdList);
                authMap.putAll(StreamUtils.toMap(deptList, dept -> "dept:" + dept.getId()
                        , SysDept::getName));
            }

            // 查询用户id对应的名称
            if (CollUtil.isNotEmpty(userIdList)) {
                List<User> userList = warmFlowMapper.selectUserByIds(userIdList);
                authMap.putAll(StreamUtils.toMap(userList, user -> String.valueOf(user.getId())
//                        , User::getName));
                        , User::getType));
            }

            // 遍历storageIds，按照原本的顺序回显名称
            for (String storageId : storageIds) {
                handlerFeedBackVos.add(new HandlerFeedBackVo(storageId
                        , MapUtil.isEmpty(authMap) ? "": authMap.get(storageId)));
            }
        }

        return handlerFeedBackVos;
    }

    /**
     * 获取角色列表
     *
     * @param query 查询条件
     * @return HandlerSelectVo
     */
    private HandlerSelectVo getRole(HandlerQuery query) {
//        startPage();
//        Role role = new Role();
//        role.setCode(query.getHandlerCode());
//        role.setName(query.getHandlerName());
        QueryWrapper<SysRole> wrapper = Wrappers.query();
        wrapper.eq("code",query.getHandlerCode());
        wrapper.eq("name",query.getHandlerName());
//        role.getParams.put("beginTime", query.getBeginTime());
//        role.getParams().put("endTime", query.getEndTime());
        // 查询角色列表
        List<SysRole> roleList = roleMapper.selectList(wrapper);
        long total = roleList.size();

        // 业务系统数据，转成组件内部能够显示的数据, total是业务数据总数，用于分页显示
        HandlerFunDto<SysRole> handlerFunDto = new HandlerFunDto<>(roleList, total)
                // 以下设置获取内置变量的Function
                .setStorageId(role1 -> "role:" + role1.getId()) // 前面拼接role:  是为了防止用户、角色的主键重复
                    .setHandlerCode(SysRole::getCode) // 权限编码
                .setHandlerName(SysRole::getName) // 权限名称
                ;

        return getHandlerSelectVo(handlerFunDto);
    }

    /**
     * 获取用户列表
     *
     * @param query 查询条件
     * @return HandlerSelectVo
     */
    private HandlerSelectVo getDept(HandlerQuery query) {
//        startPage();
//        Dept dept = new Dept();
//        dept.setName(query.getHandlerName());
//        Dept.getParams().put("beginTime", query.getBeginTime());
//        Dept.getParams().put("endTime", query.getEndTime());
        // 查询部门列表
        QueryWrapper<SysDept> wrapper = Wrappers.query();
//        wrapper.eq("code",query.getHandlerCode());
        wrapper.eq("name",query.getHandlerName());
        List<SysDept> deptList = deptMapper.selectList(wrapper);
        long total = deptList.size();

        // 业务系统数据，转成组件内部能够显示的数据, total是业务数据总数，用于分页显示
        HandlerFunDto<SysDept> handlerFunDto = new HandlerFunDto<>(deptList, total)
                .setStorageId(dept -> "dept:" + dept.getId()) // 前面拼接dept:  是为了防止用户、部门的主键重复
                .setHandlerName(SysDept::getName) // 权限名称
//                .setCreateTime(dept -> DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, dept.getCreateTime()))
                ;

        return getHandlerSelectVo(handlerFunDto);

    }

    /**
     * 获取用户列表, 同时构建左侧部门树状结构
     *
     * @param query 查询条件
     * @return HandlerSelectVo
     */
    private HandlerSelectVo getUser(HandlerQuery query) {
//        startPage();
//        User sysUser = new User();
//        sysUser.setUserName(query.getHandlerCode());
//        sysUser.setNickName(query.getHandlerName());
        // 办理人用户选择列表，需要展示左侧树状部门，所以可能会通过部门id
//        if (MathUtil.isNumeric(query.getGroupId())) {
//            sysUser.setDeptId(Long.valueOf(query.getGroupId()));
//        }
//        sysUser.getParams().put("beginTime", query.getBeginTime());
//        sysUser.getParams().put("endTime", query.getEndTime());
        // 查询用户列表
        QueryWrapper<SysUser> wrapper = Wrappers.query();
//        wrapper.eq("code",query.getHandlerCode());
        wrapper.eq("name",query.getHandlerName());
        List<SysUser> userList = userMapper.selectList(wrapper);
        long total = userList.size();
        // 查询部门列表，构建树状结构
        QueryWrapper<SysDept> deptWrapper = Wrappers.query();
        List<SysDept> deptList = deptMapper.selectList(deptWrapper);

        // 业务系统数据，转成组件内部能够显示的数据, total是业务数据总数，用于分页显示
        HandlerFunDto<SysUser> handlerFunDto = new HandlerFunDto<>(userList, total)
                .setStorageId(user -> user.getId().toString())
                .setHandlerCode(SysUser::getUsername) // 权限编码
//                .setHandlerName(User::getNickName) // 权限名称
//                .setCreateTime(user -> DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, user.getCreateTime()))
                .setGroupName(user -> String.valueOf(user.getDeptId()))
                ;

        // 业务系统机构，转成组件内部左侧树列表能够显示的数据
        TreeFunDto<SysDept> treeFunDto = new TreeFunDto<>(deptList)
                .setId(dept -> dept.getId().toString()) // 左侧树ID
                .setName(SysDept::getName) // 左侧树名称
                .setParentId(dept -> dept.getParentId().toString()); // 左侧树父级ID

        return getHandlerSelectVo(handlerFunDto, treeFunDto);
    }
}
