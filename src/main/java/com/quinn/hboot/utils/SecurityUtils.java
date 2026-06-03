package com.quinn.hboot.utils;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.quinn.hboot.system.model.vo.LoginUser;

import java.util.Collections;
import java.util.Set;

public final class SecurityUtils {

    private static final String LOGIN_USER_SESSION_KEY = "loginUser";

    private SecurityUtils() {
    }

    public static Long getUserId() {
        LoginUser user = getLoginUser();
        return user != null ? user.getUserId() : null;
    }

    public static String getUsername() {
        LoginUser user = getLoginUser();
        return user != null ? user.getUsername() : "anonymous";
    }

    public static Long getOrgId() {
        LoginUser user = getLoginUser();
        return user != null ? user.getOrgId() : null;
    }

    public static Set<String> getRoles() {
        LoginUser user = getLoginUser();
        return user != null ? user.getRoles() : Collections.emptySet();
    }

    public static Set<Long> getDeptIds() {
        LoginUser user = getLoginUser();
        return user != null ? user.getDeptIds() : Collections.emptySet();
    }

    public static Set<Long> getPostIds() {
        LoginUser user = getLoginUser();
        return user != null ? user.getPostIds() : Collections.emptySet();
    }

    public static LoginUser getLoginUser() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        Object value = StpUtil.getTokenSession().get(LOGIN_USER_SESSION_KEY);
        if (value == null) {
            return null;
        }
        if (value instanceof LoginUser loginUser) {
            return loginUser;
        }
        return JSONUtil.toBean(String.valueOf(value), LoginUser.class);
    }

    public static void setLoginUser(LoginUser loginUser) {
        if (loginUser == null) {
            return;
        }
        StpUtil.getTokenSession().set(LOGIN_USER_SESSION_KEY, JSONUtil.toJsonStr(loginUser));
    }
}
