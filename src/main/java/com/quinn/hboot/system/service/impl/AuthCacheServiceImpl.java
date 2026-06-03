package com.quinn.hboot.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.quinn.hboot.common.exception.BusinessException;
import com.quinn.hboot.config.properties.AuthProperties;
import com.quinn.hboot.system.service.AuthCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthCacheServiceImpl implements AuthCacheService {

    private final StringRedisTemplate redisTemplate;
    private final AuthProperties authProperties;

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

    private String buildLoginFailKey(String username, String clientIp) {
        return authProperties.getLoginFailPrefix() + normalizeUsername(username) + ":" + normalizeIp(clientIp);
    }

    private String normalizeUsername(String username) {
        return StrUtil.blankToDefault(StrUtil.trim(username), "unknown").toLowerCase(Locale.ROOT);
    }

    private String normalizeIp(String clientIp) {
        return StrUtil.blankToDefault(StrUtil.trim(clientIp), "unknown");
    }
}
