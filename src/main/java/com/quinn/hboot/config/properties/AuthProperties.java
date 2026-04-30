package com.quinn.hboot.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 认证与登录安全配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    /**
     * JWT 签名密钥。
     * 生产环境应放入安全配置中心或环境变量。
     */
    private String jwtSecret = "yuqinghai-hboot-438704315@qq.com";

    /**
     * JWT 令牌有效期，单位：分钟。
     */
    private long jwtExpireMinutes = 1440;

    /**
     * 登录会话在 Redis 中的有效期，单位：分钟。
     */
    private long sessionExpireMinutes = 30;

    /**
     * Redis 中登录会话前缀。
     */
    private String loginSessionPrefix = "login:";

    /**
     * Redis 中登录失败次数前缀。
     */
    private String loginFailPrefix = "login:fail:";

    /**
     * 允许连续失败的最大次数。
     */
    private int maxRetryCount = 5;

    /**
     * 达到失败阈值后的锁定时长，单位：分钟。
     */
    private long retryLockMinutes = 10;
}
