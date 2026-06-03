package com.quinn.hboot.system.service;

public interface AuthCacheService {

    void checkLoginAllowed(String username, String clientIp);

    int recordLoginFailure(String username, String clientIp);

    void clearLoginFailure(String username, String clientIp);

    int getMaxRetryCount();
}
