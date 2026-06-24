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
    private Duration flowCategory = Duration.ofDays(30);

    private long menuMaxSize = 256;
    private long menuTreeMaxSize = 256;
    private long roleMenuMaxSize = 512;
    private long dictMaxSize = 256;
    private long deptMaxSize = 256;
    private long userBaseMaxSize = 2048;
    private long userAuthMaxSize = 2048;
    private long flowCategoryMaxSize = 256;
}
