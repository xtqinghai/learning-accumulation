package com.quinn.hboot.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.quinn.hboot.common.exception.BusinessException;
import com.quinn.hboot.system.model.dto.LoginReqDTO;
import com.quinn.hboot.system.model.entity.SysMenu;
import com.quinn.hboot.system.model.vo.AuthMeVO;
import com.quinn.hboot.system.model.vo.CurrentUserVO;
import com.quinn.hboot.system.model.vo.LoginRespVO;
import com.quinn.hboot.system.model.vo.LoginUser;
import com.quinn.hboot.system.service.AuthCacheService;
import com.quinn.hboot.system.service.AuthService;
import com.quinn.hboot.system.service.JwtTokenService;
import com.quinn.hboot.system.service.LoginLogService;
import com.quinn.hboot.system.service.SysMenuService;
import com.quinn.hboot.utils.AESUtils;
import com.quinn.hboot.utils.IPUtils;
import com.quinn.hboot.utils.MenuTreeUtil;
import com.quinn.hboot.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 认证服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final AuthCacheService authCacheService;
    private final JwtTokenService jwtTokenService;
    private final SysMenuService menuService;
    private final LoginLogService loginLogService;

    @Override
    public LoginRespVO login(LoginReqDTO req, HttpServletRequest request) {
        String username = StrUtil.trim(req.getUsername());
        String clientIp = IPUtils.getIpAddr(request);

        authCacheService.checkLoginAllowed(username, clientIp);
        String plainPassword = decryptPassword(req.getPassword());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, plainPassword)
            );

            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            String tokenId = UUID.randomUUID().toString();
            String token = jwtTokenService.createToken(tokenId);

            authCacheService.cacheLoginUser(tokenId, loginUser);
            authCacheService.clearLoginFailure(username, clientIp);
            recordLoginLog(request, username, true, "登录成功");

            LoginRespVO resp = new LoginRespVO();
            resp.setToken(token);
            resp.setUser(toCurrentUser(loginUser));
            resp.setMenus(MenuTreeUtil.buildTree(menuService.findMenusByUserId(loginUser.getUserId())));
            return resp;
        } catch (DisabledException ex) {
            handleLoginFailure(request, username, clientIp, "用户已停用");
            throw new BusinessException(403, "用户已停用，请联系管理员");
        } catch (LockedException ex) {
            handleLoginFailure(request, username, clientIp, "用户已锁定");
            throw new BusinessException(403, "用户已锁定，请联系管理员");
        } catch (BadCredentialsException ex) {
            int failCount = handleLoginFailure(request, username, clientIp, "用户名或密码错误");
            throw new BusinessException(401, buildBadCredentialsMessage(failCount));
        } catch (AuthenticationException ex) {
            log.warn("用户登录认证失败，username={}，message={}", username, ex.getMessage());
            handleLoginFailure(request, username, clientIp, "登录认证失败");
            throw new BusinessException(401, "登录失败，请检查账号状态或密码");
        }
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
    public void logout(String authorizationHeader) {
        String token = resolveBearerToken(authorizationHeader);
        if (StrUtil.isBlank(token)) {
            return;
        }

        try {
            String tokenId = jwtTokenService.parseTokenId(token);
            authCacheService.removeLoginUser(tokenId);
        } catch (Exception ex) {
            log.warn("退出登录时解析 token 失败，message={}", ex.getMessage());
        }
    }

    private LoginUser requireLoginUser() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            throw new BusinessException(401, "未登录或登录状态已失效");
        }
        return loginUser;
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

    private String resolveBearerToken(String authorizationHeader) {
        if (StrUtil.isBlank(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        return authorizationHeader.substring(7).trim();
    }
}
