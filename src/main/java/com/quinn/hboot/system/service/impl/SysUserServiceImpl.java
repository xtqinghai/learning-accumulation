package com.quinn.hboot.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quinn.hboot.system.mapper.SysDeptMapper;
import com.quinn.hboot.system.mapper.SysUserMapper;
import com.quinn.hboot.system.mapper.SysUserPostMapper;
import com.quinn.hboot.system.mapper.SysUserRoleMapper;
import com.quinn.hboot.system.model.dto.RegisterUserDTO;
import com.quinn.hboot.system.model.entity.SysDept;
import com.quinn.hboot.system.model.entity.SysMenu;
import com.quinn.hboot.system.model.entity.SysUser;
import com.quinn.hboot.system.model.entity.SysUserPost;
import com.quinn.hboot.system.model.entity.SysUserRole;
import com.quinn.hboot.system.service.SysMenuService;
import com.quinn.hboot.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户服务实现。
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysMenuService menuService;
    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserPostMapper userPostMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(RegisterUserDTO dto) {
        // 先校验所属机构，避免脏数据进入用户表。
        SysDept org = deptMapper.selectById(dto.getOrgId());
        if (org == null) {
            throw new IllegalArgumentException("机构不存在");
        }

        // 用户名必须全局唯一。
        Long count = this.getBaseMapper().selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, dto.getUsername())
        );
        if (count > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname());
        user.setDeptId(dto.getOrgId());
        user.setStatus(1);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        this.getBaseMapper().insert(user);
    }

    @Override
    public SysUser getByUsername(String username) {
        return this.getBaseMapper().selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        List<SysMenu> menus = menuService.findMenusByUserId(userId);
        return menus.stream()
                .map(SysMenu::getPermission)
                .filter(Objects::nonNull)
                .filter(permission -> !permission.isBlank())
                .collect(Collectors.toList());
    }

    @Override
    public Boolean resetPwd(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("用户ID不能为空且必须为正数");
        }

        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(passwordEncoder.encode("123456"));
        return this.updateById(user);
    }

    @Override
    public List<SysUser> selectUsersByRoleIds(List<Long> longIds) {
        return userMapper.selectUsersByRoleIds(longIds);
    }

    @Override
    public List<SysUser> selectUsersByDeptIds(List<Long> longIds) {
        return userMapper.selectUsersByDeptIds(longIds);
    }

    @Override
    public List<SysUser> selectUsersByPostIds(List<Long> longIds) {
        return userMapper.selectUsersByPostIds(longIds);
    }

    @Override
    public Map<Long, String> selectUserNamesByIds(List<Long> longIds) {
        List<SysUser> list = this.baseMapper.selectByIds(longIds);
        return list.stream().collect(Collectors.toMap(SysUser::getId, SysUser::getNickname));
    }

    @Override
    public Object saveUserRolePost(SysUser entity) {
        this.baseMapper.insert(entity);

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(entity.getId());
        userRole.setRoleId(entity.getRoleId());
        userRoleMapper.insert(userRole);

        SysUserPost userPost = new SysUserPost();
        userPost.setUserId(entity.getId());
        userPost.setPostId(entity.getPostId());
        userPostMapper.insert(userPost);
        return true;
    }

    @Override
    public Object updateAllById(SysUser entity) {
        this.baseMapper.updateById(entity);
        userRoleMapper.delete(new QueryWrapper<SysUserRole>().eq("user_id", entity.getId()));
        userPostMapper.delete(new QueryWrapper<SysUserPost>().eq("user_id", entity.getId()));

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(entity.getId());
        userRole.setRoleId(entity.getRoleId());
        userRoleMapper.insert(userRole);

        SysUserPost userPost = new SysUserPost();
        userPost.setUserId(entity.getId());
        userPost.setPostId(entity.getPostId());
        userPostMapper.insert(userPost);
        return true;
    }

    @Override
    public IPage<SysUser> pageAll(Page<SysUser> objectPage, SysUser queryWrapper) {
        return this.baseMapper.selectUserPage(objectPage, queryWrapper);
    }
}
