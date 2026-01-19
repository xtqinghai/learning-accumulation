package com.quinn.hboot.system.service;

import com.quinn.hboot.system.model.entity.SysUser;
import com.quinn.hboot.system.model.vo.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @program: h-boot
 * @ClassName CustomUserDetailsService
 * @description: 说明：TODO
 * @author: yuqh
 * @create: 2026-01-17 19:50
 * @Version 1.0
 **/
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserService userService;
    private final SysMenuService menuService;

    @Override
    public UserDetails loadUserByUsername(String username) {
        SysUser user = userService.getByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        List<String> perms = userService.getUserPermissions(user.getId());

        Set<GrantedAuthority> authorities = perms.stream()
                .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setPassword(user.getPassword());
        loginUser.setOrgId(user.getDeptId());
        loginUser.setOrgName(user.getDeptId()+"");
        loginUser.setPermissions(new HashSet<>(perms));
        loginUser.setAuthorities(authorities);

        return loginUser;
    }
}

