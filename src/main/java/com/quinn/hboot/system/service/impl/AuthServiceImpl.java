package com.quinn.hboot.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.quinn.hboot.common.exception.BusinessException;
import com.quinn.hboot.common.trace.TraceContext;
import com.quinn.hboot.system.cache.SystemCacheSupport;
import com.quinn.hboot.system.model.dto.LoginReqDTO;
import com.quinn.hboot.system.model.entity.SysMenu;
import com.quinn.hboot.system.model.vo.AuthMeVO;
import com.quinn.hboot.system.model.vo.CurrentUserVO;
import com.quinn.hboot.system.model.vo.LoginRespVO;
import com.quinn.hboot.system.model.vo.LoginUser;
import com.quinn.hboot.system.service.AuthCacheService;
import com.quinn.hboot.system.service.AuthService;
import com.quinn.hboot.system.service.LoginLogService;
import com.quinn.hboot.system.service.LoginUserResolver;
import com.quinn.hboot.system.service.SysMenuService;
import com.quinn.hboot.utils.AESUtils;
import com.quinn.hboot.utils.IPUtils;
import com.quinn.hboot.utils.MenuTreeUtil;
import com.quinn.hboot.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final LoginUserResolver loginUserResolver;
    private final AuthCacheService authCacheService;
    private final SystemCacheSupport cacheSupport;
    private final SysMenuService menuService;
    private final LoginLogService loginLogService;

    @Override
    public LoginRespVO login(LoginReqDTO req, HttpServletRequest request) {
        String username = StrUtil.trim(req.getUsername());
        String clientIp = IPUtils.getIpAddr(request);

        authCacheService.checkLoginAllowed(username, clientIp);
        String plainPassword = decryptPassword(req.getPassword());
        return doLogin(request, username, clientIp, plainPassword, false);
    }

    private LoginRespVO doLogin(
        HttpServletRequest request,
        String username,
        String clientIp,
        String plainPassword,
        boolean cacheRetried
    ) {

        try {
            LoginUser loginUser = loginUserResolver.loadByUsername(username);
            if (loginUser == null || !passwordEncoder.matches(plainPassword, loginUser.getPassword())) {
                int failCount = handleLoginFailure(request, username, clientIp, "用户名或密码错误");
                throw new BusinessException(401, buildBadCredentialsMessage(failCount));
            }
            if (!loginUser.isEnabled()) {
                handleLoginFailure(request, username, clientIp, "用户已停用");
                throw new BusinessException(403, "用户已停用，请联系管理员");
            }
            if (!loginUser.isAccountNonLocked()) {
                handleLoginFailure(request, username, clientIp, "用户已锁定");
                throw new BusinessException(403, "用户已锁定，请联系管理员");
            }

            StpUtil.login(loginUser.getUserId());
            SecurityUtils.setLoginUser(loginUser);

            authCacheService.clearLoginFailure(username, clientIp);
            recordLoginLog(request, username, true, "登录成功");
            log.info("auth login success traceId={} username={} ip={}", TraceContext.getTraceId(), username, clientIp);

            LoginRespVO resp = new LoginRespVO();
            resp.setToken(StpUtil.getTokenValue());
            resp.setUser(toCurrentUser(loginUser));
            resp.setMenus(MenuTreeUtil.buildTree(menuService.findMenusByUserId(loginUser.getUserId())));
            return resp;
        } catch (BusinessException ex) {
            log.warn(
                "auth login rejected traceId={} username={} ip={} code={} message={}",
                TraceContext.getTraceId(),
                username,
                clientIp,
                ex.getCode(),
                ex.getMessage()
            );
            throw ex;
        } catch (Exception ex) {
            if (!cacheRetried) {
                clearLoginRelatedCaches();
                log.warn(
                    "auth login retry after cache clear traceId={} username={} ip={} exceptionType={} message={}",
                    TraceContext.getTraceId(),
                    username,
                    clientIp,
                    ex.getClass().getName(),
                    ex.getMessage()
                );
                return doLogin(request, username, clientIp, plainPassword, true);
            }
            log.error(
                "auth login failed traceId={} username={} ip={} exceptionType={} message={}",
                TraceContext.getTraceId(),
                username,
                clientIp,
                ex.getClass().getName(),
                ex.getMessage(),
                ex
            );
            handleLoginFailure(request, username, clientIp, "登录认证失败");
            throw new BusinessException(401, "登录失败，请检查账号状态或密码");
        }
    }

    private void clearLoginRelatedCaches() {
        cacheSupport.clearUserBaseCaches();
        cacheSupport.clearUserAuthorizationCaches();
        cacheSupport.clearRoleMenuCaches();
        cacheSupport.clearMenuCaches();
    }

    @Override
    public AuthMeVO me() {
        LoginUser loginUser = requireLoginUser();

        AuthMeVO resp = new AuthMeVO();
        resp.setUser(toCurrentUser(loginUser));
        resp.setPermissions(defaultIfNull(loginUser.getPermissions()));
        resp.setMenus(MenuTreeUtil.buildTree(menuService.findMenusByUserId(loginUser.getUserId())));
        return resp;
    }

    @Override
    public List<SysMenu> menus() {
        LoginUser loginUser = requireLoginUser();
        return MenuTreeUtil.buildTree(menuService.findMenusByUserId(loginUser.getUserId()));
    }

    @Override
    public void logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }

    private LoginUser requireLoginUser() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            Long userId = parseCurrentLoginId();
            loginUser = loginUserResolver.loadByUserId(userId);
            if (loginUser != null) {
                SecurityUtils.setLoginUser(loginUser);
            }
        }
        if (loginUser == null) {
            throw new BusinessException(401, "未登录或登录状态已失效");
        }
        return loginUser;
    }

    private Long parseCurrentLoginId() {
        Object loginId = StpUtil.getLoginIdDefaultNull();
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

    private String decryptPassword(String encryptedPassword) {
        try {
            return AESUtils.decrypt(encryptedPassword);
        } catch (RuntimeException ex) {
            throw new BusinessException(400, "登录请求无效，请重新输入账号和密码");
        }
    }

    private int handleLoginFailure(HttpServletRequest request, String username, String clientIp, String msg) {
        int failCount = authCacheService.recordLoginFailure(username, clientIp);
        recordLoginLog(request, username, false, msg);
        return failCount;
    }

    private String buildBadCredentialsMessage(int currentFailCount) {
        int remainCount = Math.max(authCacheService.getMaxRetryCount() - currentFailCount, 0);
        if (remainCount <= 0) {
            return "登录失败次数过多，请稍后再试";
        }
        return "账号或密码错误，还可尝试 " + remainCount + " 次";
    }

    private void recordLoginLog(HttpServletRequest request, String username, boolean success, String msg) {
        try {
            loginLogService.record(request, username, success, msg);
        } catch (Exception ex) {
            log.warn("记录登录日志失败，username={}，message={}", username, ex.getMessage());
        }
    }

    private CurrentUserVO toCurrentUser(LoginUser loginUser) {
        CurrentUserVO currentUser = new CurrentUserVO();
        currentUser.setId(loginUser.getUserId());
        currentUser.setUserId(loginUser.getUserId());
        currentUser.setUsername(loginUser.getUsername());
        currentUser.setNickname(loginUser.getNickname());
        currentUser.setOrgId(loginUser.getOrgId());
        currentUser.setOrgName(loginUser.getOrgName());
        currentUser.setMainPostId(loginUser.getMainPostId());
        currentUser.setDeptIds(loginUser.getDeptIds());
        currentUser.setPostIds(loginUser.getPostIds());
        currentUser.setRoles(defaultIfNull(loginUser.getRoles()));
        currentUser.setPermissions(defaultIfNull(loginUser.getPermissions()));
        return currentUser;
    }

    private Set<String> defaultIfNull(Set<String> source) {
        return source == null ? Collections.emptySet() : source;
    }
}
