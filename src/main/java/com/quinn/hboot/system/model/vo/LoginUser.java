package com.quinn.hboot.system.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * 登录态用户对象。
 * <p>
 * 该对象用于：
 * 1. Spring Security 认证上下文。
 * 2. Redis 中的登录会话缓存。
 */
@Data
public class LoginUser implements UserDetails {

    private Long userId;
    private String username;
    private String nickname;

    /**
     * 仅用于认证校验和 Redis 会话缓存，不对前端直接暴露。
     */
    @JsonIgnore
    private String password;

    /**
     * 所属机构。
     */
    private Long orgId;
    private String orgName;

    /**
     * 角色编码集合。
     */
    private Set<String> roles;

    /**
     * 权限编码集合。
     */
    private Set<String> permissions;

    /**
     * Spring Security 权限集合。
     */
    @JsonIgnore
    private Collection<? extends GrantedAuthority> authorities;

    @JsonIgnore
    private boolean accountNonExpired = true;

    @JsonIgnore
    private boolean accountNonLocked = true;

    @JsonIgnore
    private boolean credentialsNonExpired = true;

    @JsonIgnore
    private boolean enabled = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities == null ? Collections.emptyList() : authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public Set<String> getRoles() {
        return roles == null ? Collections.emptySet() : roles;
    }

    public Set<String> getPermissions() {
        return permissions == null ? Collections.emptySet() : permissions;
    }
}
