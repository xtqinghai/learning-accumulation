package com.quinn.hboot.system.service;

import com.quinn.hboot.system.model.entity.SysUser;
import com.quinn.hboot.system.model.entity.SysUserOrgPost;
import com.quinn.hboot.system.model.vo.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LoginUserResolver {

    private final SysUserService userService;

    public LoginUser loadByUsername(String username) {
        SysUser user = userService.getByUsername(username);
        return user == null ? null : buildLoginUser(user);
    }

    public LoginUser loadByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        SysUser user = userService.getUserDetail(userId);
        return user == null ? null : buildLoginUser(user);
    }

    private LoginUser buildLoginUser(SysUser user) {
        List<String> perms = userService.getUserPermissions(user.getId());
        Set<String> roleCodes = userService.getUserRoleCodes(user.getId());
        List<SysUserOrgPost> orgPosts = userService.getUserOrgPosts(user.getId());

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setNickname(user.getNickname());
        loginUser.setPassword(user.getPassword());
        loginUser.setOrgId(user.getDeptId());
        loginUser.setOrgName(user.getDeptName());
        loginUser.setMainPostId(user.getPostId());
        loginUser.setDeptIds(orgPosts.stream().map(SysUserOrgPost::getDeptId).filter(Objects::nonNull).collect(Collectors.toSet()));
        loginUser.setPostIds(orgPosts.stream().map(SysUserOrgPost::getPostId).filter(Objects::nonNull).collect(Collectors.toSet()));
        loginUser.setRoles(roleCodes.isEmpty() ? Collections.emptySet() : roleCodes);
        loginUser.setPermissions(perms.isEmpty() ? Collections.emptySet() : Set.copyOf(perms));
        loginUser.setEnabled(Objects.equals(user.getStatus(), 1));
        loginUser.setAccountNonLocked(true);
        return loginUser;
    }
}
