package com.quinn.hboot.system.model.vo;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

/**
 * @program: h-boot
 * @ClassName LoginUser
 * @description: 说明：TODO
 * @author: yuqh
 * @create: 2026-01-17 10:21
 * @Version 1.0
 **/
@Data
public class LoginUser implements UserDetails {

    private Long userId;
    private String username;
    private String password;

    /** 所属机构 */
    private Long orgId;
    private String orgName;

    /** 角色编码集合 */
    private Set<String> roles;

    /** 权限集合（可选） */
    private Set<String> permissions;

    /** Spring Security 权限 */
    private Collection<? extends GrantedAuthority> authorities;

    // ================= UserDetails 必须实现的方法 =================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

    // ================= 业务 getter =================

    public Long getUserId() { return userId; }
    public Long getOrgId() { return orgId; }
    public String getOrgName() { return orgName; }
    public Set<String> getRoles() { return roles; }
}

