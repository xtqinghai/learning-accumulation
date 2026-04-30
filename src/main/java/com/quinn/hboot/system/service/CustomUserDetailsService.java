package com.quinn.hboot.system.service;

import com.quinn.hboot.system.model.entity.SysUser;
import com.quinn.hboot.system.model.vo.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security 用户加载服务。
 * <p>
 * 负责把系统用户对象转换为登录态对象 {@link LoginUser}，
 * 同时在这里补齐权限、状态等安全上下文信息。
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) {
        SysUser user = userService.getByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        List<String> perms = userService.getUserPermissions(user.getId());
        Set<GrantedAuthority> authorities = perms.stream()
                .filter(Objects::nonNull)
                .filter(permission -> !permission.isBlank())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setNickname(user.getNickname());
        loginUser.setPassword(user.getPassword());
        loginUser.setOrgId(user.getDeptId());
        loginUser.setOrgName(user.getDeptName());
        loginUser.setRoles(Collections.emptySet());
        loginUser.setPermissions(Set.copyOf(perms));
        loginUser.setAuthorities(authorities);
        loginUser.setEnabled(Objects.equals(user.getStatus(), 1));
        loginUser.setAccountNonLocked(true);
        loginUser.setAccountNonExpired(true);
        loginUser.setCredentialsNonExpired(true);

        return loginUser;
    }
}
