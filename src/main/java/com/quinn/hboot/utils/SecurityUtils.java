package com.quinn.hboot.utils;

import com.quinn.hboot.system.model.entity.SysUser;
import com.quinn.hboot.system.model.vo.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * @author yu-qh
 */
@Component
public class SecurityUtils {

    private static LoginUser getPrincipal() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof LoginUser)) {
            return null;
        }
        return (LoginUser) authentication.getPrincipal();
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

    public static LoginUser getLoginUser() {
        return getPrincipal();
    }
}
