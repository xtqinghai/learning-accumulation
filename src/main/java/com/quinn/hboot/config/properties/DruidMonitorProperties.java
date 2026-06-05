package com.quinn.hboot.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.druid.monitor")
public class DruidMonitorProperties {

    private boolean enabled;
    private String username;
    private String password;
    private String allow;
    private String deny;
    private boolean resetEnable = false;
    private boolean sessionStatEnable = true;
    private int sessionStatMaxCount = 1000;
    private boolean profileEnable = false;
    private boolean logSlowSql = true;
    private boolean mergeSql = true;
    private long slowSqlMillis = 2000L;
    private boolean multiStatementAllow = false;
}
