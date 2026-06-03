package com.quinn.hboot.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    /**
     * Max consecutive login failures before temporary lock.
     */
    private int maxRetryCount = 5;

    /**
     * Temporary lock duration in minutes after reaching the retry threshold.
     */
    private long retryLockMinutes = 10;

    /**
     * Redis key prefix for failed login counters.
     */
    private String loginFailPrefix = "login:fail:";
}
