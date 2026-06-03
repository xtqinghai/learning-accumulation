package com.quinn.hboot.config;

import cn.dev33.satoken.stp.StpInterface;
import com.quinn.hboot.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SaTokenPermissionService implements StpInterface {

    private final SysUserService userService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = parseLoginId(loginId);
        if (userId == null) {
            return Collections.emptyList();
        }
        return userService.getUserPermissions(userId);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = parseLoginId(loginId);
        if (userId == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(userService.getUserRoleCodes(userId));
    }

    private Long parseLoginId(Object loginId) {
        if (loginId == null) {
            return null;
        }
        if (loginId instanceof Long value) {
            return value;
        }
        if (loginId instanceof Number value) {
            return value.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(loginId));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
