package com.quinn.hboot.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quinn.hboot.system.mapper.SysDeptMapper;
import com.quinn.hboot.system.mapper.SysPostMapper;
import com.quinn.hboot.system.mapper.SysRoleMapper;
import com.quinn.hboot.system.mapper.SysUserMapper;
import com.quinn.hboot.system.mapper.SysUserOrgPostMapper;
import com.quinn.hboot.system.mapper.SysUserPostMapper;
import com.quinn.hboot.system.mapper.SysUserRoleMapper;
import com.quinn.hboot.system.model.dto.RegisterUserDTO;
import com.quinn.hboot.system.model.entity.SysDept;
import com.quinn.hboot.system.model.entity.SysMenu;
import com.quinn.hboot.system.model.entity.SysPost;
import com.quinn.hboot.system.model.entity.SysRole;
import com.quinn.hboot.system.model.entity.SysUser;
import com.quinn.hboot.system.model.entity.SysUserOrgPost;
import com.quinn.hboot.system.model.entity.SysUserPost;
import com.quinn.hboot.system.model.entity.SysUserRole;
import com.quinn.hboot.system.service.SysMenuService;
import com.quinn.hboot.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysMenuService menuService;
    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final SysRoleMapper roleMapper;
    private final SysPostMapper postMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserPostMapper userPostMapper;
    private final SysUserOrgPostMapper userOrgPostMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(RegisterUserDTO dto) {
        SysDept org = deptMapper.selectById(dto.getOrgId());
        if (org == null) {
            throw new IllegalArgumentException("机构不存在");
        }

        Long count = this.getBaseMapper().selectCount(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername())
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
        SysUser user = this.getBaseMapper().selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
        if (user == null) {
            return null;
        }
        return enrichUser(user);
    }

    @Override
    public SysUser getUserDetail(Long id) {
        SysUser user = userMapper.selectUserDetail(id);
        if (user == null) {
            return null;
        }
        return enrichUser(user);
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        List<SysMenu> menus = menuService.findMenusByUserId(userId);
        return menus.stream()
            .map(SysMenu::getPermission)
            .filter(Objects::nonNull)
            .filter(permission -> !permission.isBlank())
            .distinct()
            .toList();
    }

    @Override
    public Set<String> getUserRoleCodes(Long userId) {
        List<Long> roleIds = getUserRoleIds(userId);
        if (CollectionUtils.isEmpty(roleIds)) {
            return Set.of();
        }
        return roleMapper.selectBatchIds(roleIds).stream()
            .map(SysRole::getCode)
            .filter(Objects::nonNull)
            .filter(code -> !code.isBlank())
            .collect(Collectors.toSet());
    }

    @Override
    public List<Long> getUserRoleIds(Long userId) {
        return userRoleMapper.selectList(new QueryWrapper<SysUserRole>().eq("user_id", userId)).stream()
            .map(SysUserRole::getRoleId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    @Override
    public List<SysUserOrgPost> getUserOrgPosts(Long userId) {
        List<SysUserOrgPost> orgPosts = userOrgPostMapper.selectList(
            new QueryWrapper<SysUserOrgPost>().eq("user_id", userId).orderByDesc("is_primary").orderByAsc("id")
        );
        if (CollectionUtils.isEmpty(orgPosts)) {
            return List.of();
        }

        Map<Long, String> deptNameMap = deptMapper.selectBatchIds(
            orgPosts.stream().map(SysUserOrgPost::getDeptId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(SysDept::getId, SysDept::getName));

        Map<Long, String> postNameMap = postMapper.selectBatchIds(
            orgPosts.stream().map(SysUserOrgPost::getPostId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(SysPost::getId, SysPost::getPostName));

        orgPosts.forEach(item -> {
            item.setDeptName(deptNameMap.get(item.getDeptId()));
            item.setPostName(postNameMap.get(item.getPostId()));
        });
        return orgPosts;
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
    public List<SysUser> selectUsersByRoleIds(List<Long> roleIds) {
        return userMapper.selectUsersByRoleIds(roleIds).stream().map(this::enrichUser).toList();
    }

    @Override
    public List<SysUser> selectUsersByDeptIds(List<Long> deptIds) {
        return userMapper.selectUsersByDeptIds(deptIds).stream().map(this::enrichUser).toList();
    }

    @Override
    public List<SysUser> selectUsersByPostIds(List<Long> postIds) {
        return userMapper.selectUsersByPostIds(postIds).stream().map(this::enrichUser).toList();
    }

    @Override
    public Map<Long, String> selectUserNamesByIds(List<Long> userIds) {
        List<SysUser> list = this.baseMapper.selectByIds(userIds);
        return list.stream().collect(Collectors.toMap(SysUser::getId, SysUser::getNickname));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object saveUserRolePost(SysUser entity) {
        normalizeBeforeSave(entity, true);
        this.baseMapper.insert(entity);
        replaceUserRoles(entity.getId(), entity.getRoleIds());
        replaceUserOrgPosts(entity.getId(), entity.getOrgPosts());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object updateAllById(SysUser entity) {
        normalizeBeforeSave(entity, false);
        this.baseMapper.updateById(entity);
        replaceUserRoles(entity.getId(), entity.getRoleIds());
        replaceUserOrgPosts(entity.getId(), entity.getOrgPosts());
        return true;
    }

    @Override
    public IPage<SysUser> pageAll(Page<SysUser> page, SysUser queryWrapper) {
        IPage<SysUser> result = this.baseMapper.selectUserPage(page, queryWrapper);
        if (result.getRecords() != null) {
            result.setRecords(result.getRecords().stream().map(this::enrichUser).toList());
        }
        return result;
    }

    private SysUser enrichUser(SysUser user) {
        if (user == null) {
            return null;
        }

        if (user.getDeptId() != null && user.getDeptName() == null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) {
                user.setDeptName(dept.getName());
            }
        }

        List<Long> roleIds = getUserRoleIds(user.getId());
        user.setRoleIds(roleIds);

        if (!CollectionUtils.isEmpty(roleIds)) {
            Map<Long, SysRole> roleMap = roleMapper.selectBatchIds(roleIds).stream()
                .collect(Collectors.toMap(SysRole::getId, Function.identity()));
            user.setRoleName(roleIds.stream()
                .map(roleMap::get)
                .filter(Objects::nonNull)
                .map(SysRole::getName)
                .collect(Collectors.joining(", ")));
            if (user.getRoleId() == null) {
                user.setRoleId(roleIds.get(0));
            }
        }

        List<SysUserOrgPost> orgPosts = getUserOrgPosts(user.getId());
        user.setOrgPosts(orgPosts);
        if (!CollectionUtils.isEmpty(orgPosts)) {
            user.setPostName(orgPosts.stream()
                .map(SysUserOrgPost::getPostName)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining(", ")));
            SysUserOrgPost primary = orgPosts.stream()
                .filter(item -> Objects.equals(item.getIsPrimary(), 1))
                .findFirst()
                .orElse(orgPosts.get(0));
            user.setDeptId(primary.getDeptId());
            user.setPostId(primary.getPostId());
            if (user.getDeptName() == null) {
                user.setDeptName(primary.getDeptName());
            }
        }
        return user;
    }

    private void normalizeBeforeSave(SysUser entity, boolean create) {
        if (!CollectionUtils.isEmpty(entity.getRoleIds())) {
            entity.setRoleIds(entity.getRoleIds().stream().filter(Objects::nonNull).distinct().toList());
        } else if (entity.getRoleId() != null) {
            entity.setRoleIds(List.of(entity.getRoleId()));
        } else {
            entity.setRoleIds(new ArrayList<>());
        }

        List<SysUserOrgPost> orgPosts = entity.getOrgPosts();
        if (CollectionUtils.isEmpty(orgPosts) && entity.getDeptId() != null && entity.getPostId() != null) {
            SysUserOrgPost primary = new SysUserOrgPost();
            primary.setDeptId(entity.getDeptId());
            primary.setPostId(entity.getPostId());
            primary.setIsPrimary(1);
            primary.setJobType("main");
            primary.setStatus(1);
            primary.setStartTime(LocalDateTime.now());
            orgPosts = List.of(primary);
        }

        List<SysUserOrgPost> normalized = new ArrayList<>();
        if (!CollectionUtils.isEmpty(orgPosts)) {
            for (SysUserOrgPost item : orgPosts) {
                if (item.getDeptId() == null || item.getPostId() == null) {
                    continue;
                }
                SysUserOrgPost copy = new SysUserOrgPost();
                copy.setDeptId(item.getDeptId());
                copy.setPostId(item.getPostId());
                copy.setIsPrimary(Objects.equals(item.getIsPrimary(), 1) ? 1 : 0);
                copy.setJobType(item.getJobType() == null || item.getJobType().isBlank() ? "main" : item.getJobType());
                copy.setStatus(item.getStatus() == null ? 1 : item.getStatus());
                copy.setStartTime(item.getStartTime() == null ? LocalDateTime.now() : item.getStartTime());
                copy.setEndTime(item.getEndTime());
                normalized.add(copy);
            }
        }

        if (!normalized.isEmpty() && normalized.stream().noneMatch(item -> Objects.equals(item.getIsPrimary(), 1))) {
            normalized.get(0).setIsPrimary(1);
        }
        entity.setOrgPosts(normalized);

        if (!normalized.isEmpty()) {
            SysUserOrgPost primary = normalized.stream()
                .filter(item -> Objects.equals(item.getIsPrimary(), 1))
                .findFirst()
                .orElse(normalized.get(0));
            entity.setDeptId(primary.getDeptId());
            entity.setPostId(primary.getPostId());
        }

        if (!entity.getRoleIds().isEmpty()) {
            entity.setRoleId(entity.getRoleIds().get(0));
        } else {
            entity.setRoleId(null);
        }

        if (create) {
            if (entity.getPassword() == null || entity.getPassword().isBlank()) {
                entity.setPassword(passwordEncoder.encode("123456"));
            } else if (!entity.getPassword().startsWith("$2a$")) {
                entity.setPassword(passwordEncoder.encode(entity.getPassword()));
            }
        } else if (entity.getPassword() != null && !entity.getPassword().isBlank() && !entity.getPassword().startsWith("$2a$")) {
            entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        } else if (entity.getPassword() == null || entity.getPassword().isBlank()) {
            entity.setPassword(null);
        }
    }

    private void replaceUserRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new QueryWrapper<SysUserRole>().eq("user_id", userId));
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }
        for (Long roleId : new HashSet<>(roleIds)) {
            if (roleId == null) {
                continue;
            }
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }

    private void replaceUserOrgPosts(Long userId, List<SysUserOrgPost> orgPosts) {
        userPostMapper.delete(new QueryWrapper<SysUserPost>().eq("user_id", userId));
        userOrgPostMapper.delete(new QueryWrapper<SysUserOrgPost>().eq("user_id", userId));
        if (CollectionUtils.isEmpty(orgPosts)) {
            return;
        }
        Set<Long> insertedPostIds = new HashSet<>();
        for (SysUserOrgPost item : orgPosts) {
            item.setId(null);
            item.setUserId(userId);
            userOrgPostMapper.insert(item);

            if (insertedPostIds.add(item.getPostId())) {
                SysUserPost userPost = new SysUserPost();
                userPost.setUserId(userId);
                userPost.setPostId(item.getPostId());
                userPostMapper.insert(userPost);
            }
        }
    }
}
