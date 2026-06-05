package com.quinn.hboot.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "app.cache")
public class AppCacheProperties {

    private Duration menu = Duration.ofMinutes(30);
    private Duration menuTree = Duration.ofMinutes(30);
    private Duration roleMenu = Duration.ofMinutes(30);
    private Duration dict = Duration.ofHours(1);
    private Duration dept = Duration.ofMinutes(30);
    private Duration userBase = Duration.ofMinutes(20);
    private Duration userAuth = Duration.ofMinutes(20);
}
