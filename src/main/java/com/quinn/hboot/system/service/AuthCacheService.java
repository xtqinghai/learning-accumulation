package com.quinn.hboot.system.service;

import com.quinn.hboot.system.model.vo.LoginUser;

/**
 * 登录缓存与安全控制服务。
 */
public interface AuthCacheService {

    /**
     * 缓存登录用户会话。
     */
    void cacheLoginUser(String tokenId, LoginUser loginUser);

    /**
     * 读取登录用户会话，并自动刷新会话过期时间。
     */
    LoginUser getLoginUser(String tokenId);

    /**
     * 删除登录会话。
     */
    void removeLoginUser(String tokenId);

    /**
     * 检查当前用户/IP 是否允许继续尝试登录。
     */
    void checkLoginAllowed(String username, String clientIp);

    /**
     * 记录一次登录失败，返回当前失败次数。
     */
    int recordLoginFailure(String username, String clientIp);

    /**
     * 清理登录失败计数。
     */
    void clearLoginFailure(String username, String clientIp);

    /**
     * 获取登录失败阈值。
     */
    int getMaxRetryCount();
}
