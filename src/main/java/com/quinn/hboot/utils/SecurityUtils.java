package com.quinn.hboot.utils;

import com.quinn.hboot.system.model.vo.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Set;

/**
 * 安全上下文工具类。
 * <p>
 * 统一封装当前登录用户的读取逻辑，避免业务代码直接操作
 * {@link SecurityContextHolder}。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    private static LoginUser getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            return null;
        }
        return loginUser;
    }

    public static Long getUserId() {
        LoginUser user = getPrincipal();
        return user != null ? user.getUserId() : null;
    }

    public static String getUsername() {
        LoginUser user = getPrincipal();
        return user != null ? user.getUsername() : "anonymous";
    }

    public static Long getOrgId() {
        LoginUser user = getPrincipal();
        return user != null ? user.getOrgId() : null;
    }

    public static Set<String> getRoles() {
        LoginUser user = getPrincipal();
        return user != null ? user.getRoles() : Collections.emptySet();
    }

    public static Set<Long> getDeptIds() {
        LoginUser user = getPrincipal();
        return user != null && user.getDeptIds() != null ? user.getDeptIds() : Collections.emptySet();
    }

    public static Set<Long> getPostIds() {
        LoginUser user = getPrincipal();
        return user != null && user.getPostIds() != null ? user.getPostIds() : Collections.emptySet();
    }

    public static LoginUser getLoginUser() {
        return getPrincipal();
    }
}
