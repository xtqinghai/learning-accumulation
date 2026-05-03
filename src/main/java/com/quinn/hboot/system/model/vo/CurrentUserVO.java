package com.quinn.hboot.system.model.vo;

import lombok.Data;

import java.util.Set;

/**
 * 当前登录用户对外视图。
 * <p>
 * 该对象仅返回前端需要的安全字段，不暴露密码和认证细节。
 */
@Data
public class CurrentUserVO {

    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private Long orgId;
    private String orgName;
    private Long mainPostId;
    private Set<Long> deptIds;
    private Set<Long> postIds;
    private Set<String> roles;
    private Set<String> permissions;
}
