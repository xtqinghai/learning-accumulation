package com.quinn.hboot.system.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

@Data
public class LoginUser implements Serializable {

    private Long userId;
    private String username;
    private String nickname;
    private String password;
    private Long orgId;
    private String orgName;
    private Long mainPostId;
    private Set<Long> deptIds;
    private Set<Long> postIds;
    private Set<String> roles;
    private Set<String> permissions;
    private boolean enabled = true;
    private boolean accountNonLocked = true;

    public Set<String> getRoles() {
        return roles == null ? Collections.emptySet() : roles;
    }

    public Set<String> getPermissions() {
        return permissions == null ? Collections.emptySet() : permissions;
    }

    public Set<Long> getDeptIds() {
        return deptIds == null ? Collections.emptySet() : deptIds;
    }

    public Set<Long> getPostIds() {
        return postIds == null ? Collections.emptySet() : postIds;
    }
}
