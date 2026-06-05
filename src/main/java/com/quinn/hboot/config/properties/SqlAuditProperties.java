package com.quinn.hboot.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.sql-audit")
public class SqlAuditProperties {

    private Integer topN = 20;
}
