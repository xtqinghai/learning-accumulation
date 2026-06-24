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

    /**
     * Session timeout in seconds. Default 2 hours.
     */
    private long sessionTimeoutSeconds = 7200;

    /**
     * Active timeout in seconds. Requests after this idle period require re-login.
     * Default 2 hours.
     */
    private long sessionActiveTimeoutSeconds = 7200;

    /**
     * Whether to allow concurrent sessions for the same account.
     */
    private boolean allowConcurrentLogin = true;

    /**
     * Whether a new login should kick out old sessions when concurrent login is disabled.
     */
    private boolean kickoutOldSession = true;
}
