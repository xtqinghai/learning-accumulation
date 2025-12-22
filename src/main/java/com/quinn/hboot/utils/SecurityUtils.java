package com.quinn.hboot.utils;

import com.quinn.hboot.system.model.entity.SysUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * @author yu-qh
 */
@Component
public class SecurityUtils {

    /**
     * 获取当前登录用户名（适配不同认证类型）
     * @return 用户名（未登录返回"anonymous"）
     */
    public static String getCurrentUsername() {
        // 1. 获取Authentication对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. 判空：未登录/匿名访问时，authentication可能为null
        if (authentication == null) {
            return "anonymous";
        }

        // 3. 获取用户主体（Principal）
        Object principal = authentication.getPrincipal();

        // 4. 适配不同Principal类型：
        // - UserDetails：Spring Security默认的用户信息载体
        // - String：匿名用户/简单认证场景
        // - 自定义User类：项目中自定义的用户实体（需实现UserDetails）
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        } else {
            // 自定义用户实体时，按需转换（比如return ((SysUser) principal).getUsername();）
            return principal != null ? ((SysUser) principal).getUsername() : "anonymous";
        }
    }
}
