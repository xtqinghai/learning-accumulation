package com.quinn.hboot.system.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.quinn.hboot.common.exception.BusinessException;
import com.quinn.hboot.config.properties.AuthProperties;
import com.quinn.hboot.system.model.vo.LoginUser;
import com.quinn.hboot.system.service.AuthCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 登录缓存与登录失败控制实现。
 */
@Service
@RequiredArgsConstructor
public class AuthCacheServiceImpl implements AuthCacheService {

    private final StringRedisTemplate redisTemplate;
    private final AuthProperties authProperties;

    @Override
    public void cacheLoginUser(String tokenId, LoginUser loginUser) {
        JSONObject cacheObject = JSONUtil.parseObj(loginUser);
        // authorities 为接口类型，直接序列化到 Redis 后无法稳定反序列化，这里只缓存权限编码集合
        cacheObject.remove("authorities");
        redisTemplate.opsForValue().set(
                buildLoginSessionKey(tokenId),
                cacheObject.toString(),
                authProperties.getSessionExpireMinutes(),
                TimeUnit.MINUTES
        );
    }

    @Override
    public LoginUser getLoginUser(String tokenId) {
        String key = buildLoginSessionKey(tokenId);
        String json = redisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(json)) {
            return null;
        }

        redisTemplate.expire(key, authProperties.getSessionExpireMinutes(), TimeUnit.MINUTES);
        JSONObject cacheObject = JSONUtil.parseObj(json);
        // 兼容历史缓存：即使 Redis 中已存在 authorities 字段，也在反序列化前移除并重新构建
        cacheObject.remove("authorities");
        LoginUser loginUser = cacheObject.toBean(LoginUser.class);
        loginUser.setAuthorities(rebuildAuthorities(loginUser));
        return loginUser;
    }

    @Override
    public void removeLoginUser(String tokenId) {
        redisTemplate.delete(buildLoginSessionKey(tokenId));
    }

    @Override
    public void checkLoginAllowed(String username, String clientIp) {
        String key = buildLoginFailKey(username, clientIp);
        String failCountText = redisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(failCountText)) {
            return;
        }

        int failCount = Integer.parseInt(failCountText);
        if (failCount < authProperties.getMaxRetryCount()) {
            return;
        }

        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        long remainSeconds = ttl == null || ttl < 0 ? authProperties.getRetryLockMinutes() * 60 : ttl;
        throw new BusinessException(429, "登录失败次数过多，请 " + remainSeconds + " 秒后重试");
    }

    @Override
    public int recordLoginFailure(String username, String clientIp) {
        String key = buildLoginFailKey(username, clientIp);
        Long failCount = redisTemplate.opsForValue().increment(key);
        if (failCount != null && failCount == 1L) {
            redisTemplate.expire(key, authProperties.getRetryLockMinutes(), TimeUnit.MINUTES);
        }
        return failCount == null ? 0 : failCount.intValue();
    }

    @Override
    public void clearLoginFailure(String username, String clientIp) {
        redisTemplate.delete(buildLoginFailKey(username, clientIp));
    }

    @Override
    public int getMaxRetryCount() {
        return authProperties.getMaxRetryCount();
    }

    private String buildLoginSessionKey(String tokenId) {
        return authProperties.getLoginSessionPrefix() + tokenId;
    }

    private String buildLoginFailKey(String username, String clientIp) {
        return authProperties.getLoginFailPrefix() + normalizeUsername(username) + ":" + normalizeIp(clientIp);
    }

    private String normalizeUsername(String username) {
        return StrUtil.blankToDefault(StrUtil.trim(username), "unknown")
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeIp(String clientIp) {
        return StrUtil.blankToDefault(StrUtil.trim(clientIp), "unknown");
    }

    private Set<GrantedAuthority> rebuildAuthorities(LoginUser loginUser) {
        return loginUser.getPermissions().stream()
                .filter(StrUtil::isNotBlank)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
